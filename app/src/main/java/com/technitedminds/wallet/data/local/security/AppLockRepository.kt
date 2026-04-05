package com.technitedminds.wallet.data.local.security

import android.content.Context
import com.technitedminds.wallet.data.local.database.WalletDatabase
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a PIN verification attempt with rate limiting.
 */
sealed interface PinVerifyResult {
    /** PIN is correct — counters reset. */
    data object Success : PinVerifyResult

    /** PIN is wrong. [attemptsRemaining] before lockout starts (0 = lockout applied). */
    data class Wrong(val attemptsRemaining: Int) : PinVerifyResult

    /** Too many failures — user must wait [remainingSeconds] before retrying. */
    data class LockedOut(val remainingSeconds: Int) : PinVerifyResult

    /** Maximum failures reached — auto data wipe triggered. */
    data object WipeTriggered : PinVerifyResult
}

/**
 * Repository that wraps all app-lock related DataStore operations and
 * delegates hashing/verification to [PinHasher] and [RecoveryCodeManager].
 * ViewModels should depend on this instead of touching preferences directly.
 */
@Singleton
class AppLockRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: SimplePreferencesManager,
    private val pinHasher: PinHasher,
    private val recoveryCodeManager: RecoveryCodeManager,
) {

    // ── Observe states ─────────────────────────────────────────────────

    fun isAppLockEnabled(): Flow<Boolean> = preferencesManager.isAppLockEnabled()

    fun isBiometricEnabled(): Flow<Boolean> = preferencesManager.isBiometricAuthEnabled()

    fun getAppLockTimeout(): Flow<Int> = preferencesManager.getAppLockTimeout()

    fun getLastUnlockEpoch(): Flow<Long> = preferencesManager.getLastUnlockEpoch()

    /** Emits true when a PIN hash exists in storage */
    fun isPinConfigured(): Flow<Boolean> =
        combine(preferencesManager.getPinHash(), preferencesManager.getPinSalt()) { hash, salt ->
            !hash.isNullOrEmpty() && !salt.isNullOrEmpty()
        }

    // ── Onboarding ────────────────────────────────────────────────────

    fun isOnboardingCompleted(): Flow<Boolean> = preferencesManager.isOnboardingCompleted()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesManager.setOnboardingCompleted(completed)
    }

    // ── PIN management ─────────────────────────────────────────────────

    /**
     * Set (or change) the 4-digit PIN.
     * Generates a new salt, hashes the PIN, persists both, and enables the lock.
     * Also generates a recovery code and returns it.
     */
    suspend fun setupPin(pin: String): RecoveryCodeManager.GeneratedCode {
        val salt = pinHasher.generateSalt()
        val hash = pinHasher.hash(pin, salt)

        preferencesManager.setPinHash(hash)
        preferencesManager.setPinSalt(salt)
        preferencesManager.setAppLockEnabled(true)

        // Generate and store recovery code
        val recovery = recoveryCodeManager.generate()
        preferencesManager.setRecoveryCodeHash(recovery.hash)
        preferencesManager.setRecoveryCodeSalt(recovery.salt)

        return recovery
    }

    // ── Rate limiting constants ──────────────────────────────────────

    companion object {
        /** Number of wrong attempts before lockout kicks in. */
        const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        /** Maximum wrong attempts before auto data-wipe. */
        const val MAX_ATTEMPTS_BEFORE_WIPE = 10
        /** Lockout durations in seconds, indexed by lockout tier (0-based). */
        private val LOCKOUT_SECONDS = intArrayOf(30, 60, 120, 300) // 30s, 1m, 2m, 5m
    }

    /**
     * Verify an entered PIN with rate limiting.
     *
     * Returns a [PinVerifyResult]:
     * - [PinVerifyResult.Success] on correct PIN (resets counter).
     * - [PinVerifyResult.Wrong] on incorrect PIN (increments counter).
     * - [PinVerifyResult.LockedOut] if too many failures (includes remaining seconds).
     * - [PinVerifyResult.WipeTriggered] after [MAX_ATTEMPTS_BEFORE_WIPE] failures.
     */
    suspend fun verifyPin(pin: String): PinVerifyResult {
        // Check if currently locked out
        val lockoutUntil = preferencesManager.getLockoutUntilEpoch()
        val now = System.currentTimeMillis()
        if (lockoutUntil > now) {
            val remainingSec = ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
            return PinVerifyResult.LockedOut(remainingSec)
        }

        val storedHash = preferencesManager.getPinHash().first() ?: return PinVerifyResult.Wrong(0)
        val storedSalt = preferencesManager.getPinSalt().first() ?: return PinVerifyResult.Wrong(0)
        val correct = pinHasher.verify(pin, storedSalt, storedHash)

        return if (correct) {
            // Reset counters on success
            preferencesManager.setFailedPinAttempts(0)
            preferencesManager.setLockoutUntilEpoch(0L)
            PinVerifyResult.Success
        } else {
            val attempts = preferencesManager.getFailedPinAttempts() + 1
            preferencesManager.setFailedPinAttempts(attempts)

            if (attempts >= MAX_ATTEMPTS_BEFORE_WIPE) {
                PinVerifyResult.WipeTriggered
            } else if (attempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
                val tier = ((attempts - MAX_ATTEMPTS_BEFORE_LOCKOUT)
                    .coerceAtMost(LOCKOUT_SECONDS.lastIndex))
                val lockSeconds = LOCKOUT_SECONDS[tier]
                preferencesManager.setLockoutUntilEpoch(now + lockSeconds * 1000L)
                PinVerifyResult.LockedOut(lockSeconds)
            } else {
                PinVerifyResult.Wrong(MAX_ATTEMPTS_BEFORE_LOCKOUT - attempts)
            }
        }
    }

    /**
     * Returns the remaining lockout seconds if currently locked out, else 0.
     */
    suspend fun getRemainingLockoutSeconds(): Int {
        val lockoutUntil = preferencesManager.getLockoutUntilEpoch()
        val remaining = ((lockoutUntil - System.currentTimeMillis()) / 1000).toInt()
        return remaining.coerceAtLeast(0)
    }

    // ── Recovery code ──────────────────────────────────────────────────

    /**
     * Verify an entered recovery code.
     */
    suspend fun verifyRecoveryCode(code: String): Boolean {
        val storedHash = preferencesManager.getRecoveryCodeHash().first() ?: return false
        val storedSalt = preferencesManager.getRecoveryCodeSalt().first() ?: return false
        return recoveryCodeManager.verify(code, storedSalt, storedHash)
    }

    /**
     * Regenerate the recovery code. Returns the new plaintext code for display.
     * Invalidates the old code.
     */
    suspend fun regenerateRecoveryCode(): RecoveryCodeManager.GeneratedCode {
        val recovery = recoveryCodeManager.generate()
        preferencesManager.setRecoveryCodeHash(recovery.hash)
        preferencesManager.setRecoveryCodeSalt(recovery.salt)
        return recovery
    }

    // ── Biometric toggle ───────────────────────────────────────────────

    suspend fun setBiometricEnabled(enabled: Boolean) {
        preferencesManager.setBiometricAuthEnabled(enabled)
    }

    // ── Lock / Unlock lifecycle ────────────────────────────────────────

    suspend fun recordUnlock() {
        preferencesManager.setLastUnlockEpoch(System.currentTimeMillis())
    }

    /**
     * Decide whether the app should be locked based on the timeout setting
     * and the last unlock epoch.
     */
    suspend fun shouldLockNow(): Boolean {
        val lockEnabled = preferencesManager.isAppLockEnabled().first()
        if (!lockEnabled) return false

        val pinConfigured = isPinConfigured().first()
        if (!pinConfigured) return false

        val timeout = preferencesManager.getAppLockTimeout().first()
        val lastUnlock = preferencesManager.getLastUnlockEpoch().first()

        if (lastUnlock == 0L) return true // never unlocked → lock

        val elapsedMs = System.currentTimeMillis() - lastUnlock
        val timeoutMs = timeout * 60 * 1000L // minutes → ms; 0 = immediate
        return elapsedMs >= timeoutMs
    }

    // ── Timeout setting ────────────────────────────────────────────────

    suspend fun setAppLockTimeout(timeoutMinutes: Int) {
        preferencesManager.setAppLockTimeout(timeoutMinutes)
    }

    // ── Enable / Disable lock ──────────────────────────────────────────

    suspend fun setAppLockEnabled(enabled: Boolean) {
        preferencesManager.setAppLockEnabled(enabled)
    }

    // ── Data wipe (recovery failure path) ──────────────────────────────

    /** Clear all security preferences. Caller should also wipe the database. */
    suspend fun clearAllSecurityData() {
        preferencesManager.clearSecurityPreferences()
    }

    /**
     * Clear ALL app data: preferences, Room database, card images, thumbnails, and cache.
     * This is the nuclear option triggered by max PIN failures or manual data wipe.
     */
    suspend fun clearAllAppData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            preferencesManager.clearAllPreferences()
            WalletDatabase.wipeCompletely(context)

            listOf("card_images", "thumbnails", "temp", "logs").forEach { dirName ->
                File(context.filesDir, dirName).deleteRecursively()
            }

            File(context.cacheDir, "cache").deleteRecursively()
            File(context.cacheDir, "shared_cards").deleteRecursively()

            context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit()
            context.getSharedPreferences("cardvault_keyset_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit()
            Unit
        }
    }
}
