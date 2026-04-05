package com.technitedminds.wallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.technitedminds.wallet.presentation.components.common.SplashOverlay
import com.technitedminds.wallet.presentation.navigation.WalletAppScaffold
import com.technitedminds.wallet.presentation.screens.addcard.NfcCardReaderManager
import com.technitedminds.wallet.presentation.screens.home.EnhancedHomeScreen
import com.technitedminds.wallet.presentation.screens.onboarding.OnboardingPinScreen
import com.technitedminds.wallet.presentation.screens.security.AppLockEvent
import com.technitedminds.wallet.presentation.screens.security.AppLockScreen
import com.technitedminds.wallet.presentation.screens.security.AppLockViewModel
import com.technitedminds.wallet.presentation.screens.security.BiometricAuthManager
import com.technitedminds.wallet.presentation.screens.security.PinScreenMode
import com.technitedminds.wallet.presentation.screens.settings.SettingsViewModel
import com.technitedminds.wallet.ui.theme.SpaceEnd
import com.technitedminds.wallet.ui.theme.WalletTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Startup phase — used only for biometric trigger timing.
 * Content layers are mounted independently to avoid white flash.
 */
private enum class AppPhase {
    SPLASH,
    ONBOARDING,
    APP_LOCK,
    READY,
}

/**
 * Main activity for the CardVault wallet app.
 * Uses [FragmentActivity] for [BiometricAuthManager] compatibility.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var nfcCardReaderManager: NfcCardReaderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val appLockViewModel: AppLockViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()
            val lockState by appLockViewModel.uiState.collectAsState()

            // ── Splash animation tracking ───────────────────────────────
            var splashAnimDone by remember { mutableStateOf(false) }

            // Splash stays visible until BOTH animation finishes AND prefs load.
            val showSplash = !splashAnimDone || !lockState.isInitialized

            // Derived booleans for what content should exist behind splash
            val needsOnboarding = lockState.isInitialized && !lockState.onboardingCompleted
            val needsLock = lockState.isInitialized && lockState.onboardingCompleted &&
                lockState.isLocked && lockState.isLockEnabled && lockState.pinConfigured
            val isReady = lockState.isInitialized && lockState.onboardingCompleted && !needsLock

            // Phase is used for biometric trigger timing only
            val phase = when {
                showSplash -> AppPhase.SPLASH
                needsOnboarding -> AppPhase.ONBOARDING
                needsLock -> AppPhase.APP_LOCK
                else -> AppPhase.READY
            }

            // ── Lifecycle observer: re-lock on resume ───────────────────
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        appLockViewModel.onAppResumed()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
            }

            // ── Trigger biometric ONLY when lock screen is visible ──────
            val activity = this@MainActivity
            LaunchedEffect(phase) {
                if (phase == AppPhase.APP_LOCK && lockState.canUseBiometric) {
                    kotlinx.coroutines.delay(350)
                    appLockViewModel.requestBiometric()
                }
            }

            // ── Handle one-shot events (biometric prompt, data wipe) ────
            LaunchedEffect(Unit) {
                appLockViewModel.events.collect { event ->
                    when (event) {
                        is AppLockEvent.BiometricRequested -> {
                            biometricAuthManager.authenticate(
                                activity = activity,
                                onSuccess = { appLockViewModel.onBiometricSuccess() },
                                onFailure = { msg -> appLockViewModel.onBiometricFailure(msg) },
                            )
                        }
                        is AppLockEvent.DataWiped -> {
                            activity.recreate()
                        }
                        else -> { /* handled by UI state */ }
                    }
                }
            }

            WalletTheme(themeMode = uiState.themeMode) {
                // Dark base background — prevents ANY white flash during
                // transitions (splash fade-out, phase changes, etc.)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SpaceEnd),
                ) {
                    // ── Content layers: mounted behind splash as soon as
                    //    prefs load, so they're ready when splash fades ────

                    // Home screen — warm behind lock, or visible if ready
                    if (isReady || needsLock) {
                        WalletAppScaffold(
                            modifier = Modifier.fillMaxSize(),
                            nfcCardReaderManager = nfcCardReaderManager,
                        )
                    }

                    // Lock overlay (opaque, on top of home)
                    if (needsLock) {
                        AppLockScreen(
                            mode = PinScreenMode.Unlock(
                                uiState = lockState,
                                onDigitEntered = appLockViewModel::onDigitEntered,
                                onDeleteDigit = appLockViewModel::onDeleteDigit,
                                onBiometricClick = appLockViewModel::requestBiometric,
                                onForgotPinClick = appLockViewModel::showRecoveryInput,
                                onRecoveryCodeChanged = appLockViewModel::updateRecoveryCode,
                                onVerifyRecoveryCode = appLockViewModel::verifyRecoveryCode,
                                onHideRecovery = appLockViewModel::hideRecoveryInput,
                                onShowWipeConfirmation = appLockViewModel::showWipeConfirmation,
                                onHideWipeConfirmation = appLockViewModel::hideWipeConfirmation,
                                onConfirmWipe = appLockViewModel::confirmDataWipe,
                                onShakeComplete = appLockViewModel::clearShakeError,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    // Onboarding (opaque, first install)
                    if (needsOnboarding) {
                        OnboardingFlow(appLockViewModel = appLockViewModel)
                    }

                    // ── Splash: topmost layer, hides everything until done ──
                    if (showSplash) {
                        SplashOverlay(
                            onSplashFinished = { splashAnimDone = true },
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// OnboardingFlow — welcome screen + optional PIN setup
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingFlow(appLockViewModel: AppLockViewModel) {
    var showPinSetup by remember { mutableStateOf(false) }
    var recoveryCode by remember { mutableStateOf<String?>(null) }

    OnboardingPinScreen(
        onSetupPin = { showPinSetup = true },
        onSkip = { appLockViewModel.completeOnboarding() },
        modifier = Modifier.fillMaxSize(),
    )

    if (showPinSetup) {
        AppLockScreen(
            mode = PinScreenMode.Setup(
                onPinConfirmed = { pin ->
                    showPinSetup = false
                    appLockViewModel.setupNewPin(pin) { code ->
                        recoveryCode = code
                    }
                },
                onCancel = { showPinSetup = false },
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }

    recoveryCode?.let { code ->
        RecoveryCodeOnboardingDialog(
            code = code,
            onDone = {
                recoveryCode = null
                appLockViewModel.completeOnboarding()
            },
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// RecoveryCodeOnboardingDialog
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecoveryCodeOnboardingDialog(code: String, onDone: () -> Unit) {
    Dialog(
        onDismissRequest = { /* non-dismissible */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.Key, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text("Save Your Recovery Code", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Write this code down and keep it safe. You'll need it if you forget your PIN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = code, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        letterSpacing = 2.sp, modifier = Modifier.padding(16.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("I've Saved It", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    WalletTheme {
        EnhancedHomeScreen(onCardClick = { }, modifier = Modifier.fillMaxSize())
    }
}
