package com.technitedminds.wallet.presentation.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.data.local.security.AppLockRepository
import com.technitedminds.wallet.data.local.security.PinVerifyResult
import com.technitedminds.wallet.data.local.security.RecoveryCodeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI state ────────────────────────────────────────────────────────────

data class AppLockUiState(
    /** Whether the app-lock feature is turned on in settings */
    val isLockEnabled: Boolean = false,
    /** Whether the lock screen should be displayed right now */
    val isLocked: Boolean = true,
    /** Whether biometric hardware is present and user opted in */
    val canUseBiometric: Boolean = false,
    /** Whether a PIN has been configured already */
    val pinConfigured: Boolean = false,
    /** Current PIN digits entered (max 4) */
    val enteredPin: String = "",
    /** Transient error message (wrong PIN, etc.) */
    val errorMessage: String? = null,
    /** Trigger shake animation on the PIN dots */
    val shakeError: Boolean = false,
    /** Show the recovery-code input UI */
    val showRecoveryInput: Boolean = false,
    /** Show the "delete all data" confirmation */
    val showWipeConfirmation: Boolean = false,
    /** Recovery code entry */
    val recoveryCodeInput: String = "",
    /** Recovery code error */
    val recoveryError: String? = null,
    /** Lock timeout in minutes (0 = immediate) */
    val lockTimeout: Int = 0,
    /** Whether biometric hardware is available */
    val biometricAvailable: Boolean = false,
    /** True once preferences have been loaded — prevents flash of home screen */
    val isInitialized: Boolean = false,
    /** Whether first-install onboarding has been completed */
    val onboardingCompleted: Boolean = true, // safe default – won't block until prefs load
    /** Remaining lockout seconds (0 = not locked out) */
    val lockoutSeconds: Int = 0,
    /** Whether input is disabled due to lockout */
    val isLockedOut: Boolean = false,
)

// ── Events (one-shot) ───────────────────────────────────────────────────

sealed interface AppLockEvent {
    data object Unlocked : AppLockEvent
    data object BiometricRequested : AppLockEvent
    data class RecoveryCodeGenerated(val code: String) : AppLockEvent
    data object DataWiped : AppLockEvent
}

// ── ViewModel ───────────────────────────────────────────────────────────

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockRepository: AppLockRepository,
    private val biometricAuthManager: BiometricAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AppLockEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AppLockEvent> = _events.asSharedFlow()

    init {
        observePreferences()
        checkInitialLockState()
    }

    // ── Observe reactive preference flows ──────────────────────────────

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                appLockRepository.isAppLockEnabled(),
                appLockRepository.isBiometricEnabled(),
                appLockRepository.isPinConfigured(),
                appLockRepository.getAppLockTimeout(),
                appLockRepository.isOnboardingCompleted(),
            ) { lockEnabled, bioEnabled, pinConfigured, timeout, onboardingDone ->
                val bioAvailable = biometricAuthManager.isBiometricAvailable()
                _uiState.value = _uiState.value.copy(
                    isLockEnabled = lockEnabled,
                    canUseBiometric = bioEnabled && bioAvailable,
                    pinConfigured = pinConfigured,
                    lockTimeout = timeout,
                    biometricAvailable = bioAvailable,
                    onboardingCompleted = onboardingDone,
                    isInitialized = true,
                )
            }.collect()
        }
    }

    private fun checkInitialLockState() {
        viewModelScope.launch {
            // Wait until preferences have loaded before deciding lock state
            _uiState.first { it.isInitialized }
            val shouldLock = appLockRepository.shouldLockNow()
            _uiState.update { it.copy(isLocked = shouldLock) }

            // Resume lockout countdown if app was killed during a lockout
            if (shouldLock) {
                val remaining = appLockRepository.getRemainingLockoutSeconds()
                if (remaining > 0) {
                    _uiState.update {
                        it.copy(
                            isLockedOut = true, lockoutSeconds = remaining,
                            errorMessage = "Too many attempts — wait ${remaining}s",
                        )
                    }
                    startLockoutCountdown(remaining)
                }
            }
            // NOTE: Biometric is NOT triggered here. The UI layer triggers it
            // when the lock screen actually becomes visible (after splash).
        }
    }

    // ── Called when app comes back from background ─────────────────────

    fun onAppResumed() {
        viewModelScope.launch {
            val shouldLock = appLockRepository.shouldLockNow()
            if (shouldLock) {
                _uiState.update {
                    it.copy(
                        isLocked = true,
                        enteredPin = "",
                        errorMessage = null,
                        shakeError = false,
                    )
                }
                // NOTE: Biometric is NOT triggered here. The UI layer triggers it
                // when the lock screen becomes visible (phase == APP_LOCK).
            }
        }
    }

    // ── PIN entry ──────────────────────────────────────────────────────

    fun onDigitEntered(digit: Char) {
        if (_uiState.value.isLockedOut) return // Ignore input during lockout
        val current = _uiState.value.enteredPin
        if (current.length >= 4) return
        val newPin = current + digit
        _uiState.update { it.copy(enteredPin = newPin, errorMessage = null, shakeError = false) }

        if (newPin.length == 4) {
            verifyEnteredPin(newPin)
        }
    }

    fun onDeleteDigit() {
        if (_uiState.value.isLockedOut) return
        val current = _uiState.value.enteredPin
        if (current.isEmpty()) return
        _uiState.update { it.copy(enteredPin = current.dropLast(1), errorMessage = null, shakeError = false) }
    }

    private var lockoutCountdownJob: Job? = null

    private fun verifyEnteredPin(pin: String) {
        viewModelScope.launch {
            when (val result = appLockRepository.verifyPin(pin)) {
                is PinVerifyResult.Success -> {
                    appLockRepository.recordUnlock()
                    _uiState.update {
                        it.copy(
                            isLocked = false, enteredPin = "", errorMessage = null,
                            isLockedOut = false, lockoutSeconds = 0,
                        )
                    }
                    _events.emit(AppLockEvent.Unlocked)
                }
                is PinVerifyResult.Wrong -> {
                    val msg = if (result.attemptsRemaining > 0) {
                        "Wrong PIN — ${result.attemptsRemaining} attempts left"
                    } else {
                        "Wrong PIN"
                    }
                    _uiState.update { it.copy(enteredPin = "", errorMessage = msg, shakeError = true) }
                }
                is PinVerifyResult.LockedOut -> {
                    _uiState.update {
                        it.copy(
                            enteredPin = "", shakeError = true,
                            isLockedOut = true, lockoutSeconds = result.remainingSeconds,
                            errorMessage = "Too many attempts — wait ${result.remainingSeconds}s",
                        )
                    }
                    startLockoutCountdown(result.remainingSeconds)
                }
                is PinVerifyResult.WipeTriggered -> {
                    appLockRepository.clearAllAppData()
                    _uiState.update {
                        it.copy(
                            isLocked = false, enteredPin = "",
                            isLockedOut = false, lockoutSeconds = 0,
                        )
                    }
                    _events.emit(AppLockEvent.DataWiped)
                }
            }
        }
    }

    /**
     * Counts down the lockout timer, updating [AppLockUiState.lockoutSeconds]
     * every second, then clears the lockout.
     */
    private fun startLockoutCountdown(seconds: Int) {
        lockoutCountdownJob?.cancel()
        lockoutCountdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update {
                    it.copy(
                        lockoutSeconds = remaining,
                        errorMessage = if (remaining > 0) "Too many attempts — wait ${remaining}s" else null,
                    )
                }
            }
            _uiState.update { it.copy(isLockedOut = false, lockoutSeconds = 0, errorMessage = null) }
        }
    }

    fun clearShakeError() {
        _uiState.update { it.copy(shakeError = false) }
    }

    // ── Biometric result ───────────────────────────────────────────────

    fun onBiometricSuccess() {
        viewModelScope.launch {
            appLockRepository.recordUnlock()
            _uiState.update { it.copy(isLocked = false, enteredPin = "") }
            _events.emit(AppLockEvent.Unlocked)
        }
    }

    fun onBiometricFailure(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun requestBiometric() {
        viewModelScope.launch {
            _events.emit(AppLockEvent.BiometricRequested)
        }
    }

    // ── Recovery flow ──────────────────────────────────────────────────

    fun showRecoveryInput() {
        _uiState.update { it.copy(showRecoveryInput = true, recoveryCodeInput = "", recoveryError = null) }
    }

    fun hideRecoveryInput() {
        _uiState.update { it.copy(showRecoveryInput = false, recoveryCodeInput = "", recoveryError = null) }
    }

    fun updateRecoveryCode(code: String) {
        _uiState.update { it.copy(recoveryCodeInput = code, recoveryError = null) }
    }

    fun verifyRecoveryCode() {
        viewModelScope.launch {
            val code = _uiState.value.recoveryCodeInput
            val valid = appLockRepository.verifyRecoveryCode(code)
            if (valid) {
                appLockRepository.recordUnlock()
                _uiState.update {
                    it.copy(isLocked = false, showRecoveryInput = false, recoveryCodeInput = "")
                }
                _events.emit(AppLockEvent.Unlocked)
            } else {
                _uiState.update { it.copy(recoveryError = "Invalid recovery code") }
            }
        }
    }

    // ── Data wipe ──────────────────────────────────────────────────────

    fun showWipeConfirmation() {
        _uiState.update { it.copy(showWipeConfirmation = true) }
    }

    fun hideWipeConfirmation() {
        _uiState.update { it.copy(showWipeConfirmation = false) }
    }

    fun confirmDataWipe() {
        viewModelScope.launch {
            appLockRepository.clearAllAppData()
            _uiState.update {
                it.copy(
                    isLocked = false,
                    showRecoveryInput = false,
                    showWipeConfirmation = false,
                )
            }
            _events.emit(AppLockEvent.DataWiped)
        }
    }

    // ── Settings helpers (called from Settings screen) ─────────────────

    fun setupNewPin(pin: String, onRecoveryCode: (String) -> Unit) {
        viewModelScope.launch {
            val recovery = appLockRepository.setupPin(pin)
            onRecoveryCode(recovery.plaintextCode)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            appLockRepository.setBiometricEnabled(enabled)
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            appLockRepository.setAppLockEnabled(enabled)
            if (!enabled) {
                // Immediately unlock when disabling
                _uiState.update { it.copy(isLocked = false) }
            }
        }
    }

    fun updateLockTimeout(minutes: Int) {
        viewModelScope.launch {
            appLockRepository.setAppLockTimeout(minutes)
        }
    }

    fun regenerateRecoveryCode(onCode: (String) -> Unit) {
        viewModelScope.launch {
            val recovery = appLockRepository.regenerateRecoveryCode()
            onCode(recovery.plaintextCode)
        }
    }

    // ── Onboarding ──────────────────────────────────────────────────────

    fun completeOnboarding() {
        viewModelScope.launch {
            appLockRepository.setOnboardingCompleted(true)
            // If user just set a PIN during onboarding, don't lock them out immediately
            appLockRepository.recordUnlock()
            _uiState.update { it.copy(onboardingCompleted = true, isLocked = false) }
        }
    }
}
