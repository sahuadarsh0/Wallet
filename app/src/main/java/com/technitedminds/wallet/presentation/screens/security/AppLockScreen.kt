package com.technitedminds.wallet.presentation.screens.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.presentation.components.common.ConfirmationDialog
import com.technitedminds.wallet.presentation.components.common.ConfirmationType
import com.technitedminds.wallet.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════
// PinScreenMode — determines whether the screen is unlocking or setting
//                 up / changing a PIN
// ═══════════════════════════════════════════════════════════════════════

sealed interface PinScreenMode {

    /** App-start / resume unlock flow — driven by [AppLockUiState]. */
    data class Unlock(
        val uiState: AppLockUiState,
        val onDigitEntered: (Char) -> Unit,
        val onDeleteDigit: () -> Unit,
        val onBiometricClick: () -> Unit,
        val onForgotPinClick: () -> Unit,
        val onRecoveryCodeChanged: (String) -> Unit,
        val onVerifyRecoveryCode: () -> Unit,
        val onHideRecovery: () -> Unit,
        val onShowWipeConfirmation: () -> Unit,
        val onHideWipeConfirmation: () -> Unit,
        val onConfirmWipe: () -> Unit,
        val onShakeComplete: () -> Unit,
    ) : PinScreenMode

    /**
     * Set / change PIN flow — self-contained local state.
     * On success [onPinConfirmed] is called with the final 4-digit PIN.
     * On confirm mismatch the screen resets to a fresh step-0 entry.
     */
    data class Setup(
        val onPinConfirmed: (String) -> Unit,
        val onCancel: () -> Unit,
    ) : PinScreenMode
}

// ═══════════════════════════════════════════════════════════════════════
// AppLockScreen — single full-screen PIN surface for both modes
// ═══════════════════════════════════════════════════════════════════════

@Composable
fun AppLockScreen(
    mode: PinScreenMode,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    // ── Shared background animation ────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "bg_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_phase",
    )

    // ── Entrance ───────────────────────────────────────────────────────
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val entranceAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(500),
        label = "entrance_alpha",
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.9f,
        animationSpec = WalletSpring.gentle(),
        label = "entrance_scale",
    )

    // ── Render based on mode ───────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceEnd)
            .pointerInput(Unit) { detectTapGestures { /* consume all taps */ } }
            .drawBehind {
                val offset = shimmerPhase * size.width * 0.5f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(SpaceStart, SpaceEnd, SpaceStart.copy(alpha = 0.85f)),
                        start = Offset(offset, 0f),
                        end = Offset(size.width + offset, size.height),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(WalletPurple.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2, size.height * 0.32f),
                        radius = size.width * 0.55f,
                    ),
                )
            }
            .graphicsLayer {
                alpha = entranceAlpha
                scaleX = entranceScale
                scaleY = entranceScale
            },
        contentAlignment = Alignment.Center,
    ) {
        when (mode) {
            is PinScreenMode.Unlock -> UnlockContent(mode, haptic)
            is PinScreenMode.Setup -> SetupContent(mode, haptic)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// UnlockContent — existing unlock flow (ViewModel-driven state)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun UnlockContent(
    mode: PinScreenMode.Unlock,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    val ui = mode.uiState

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
    ) {
        Spacer(Modifier.weight(0.12f))

        BreathingLockIcon(icon = Icons.Default.Lock)

        Spacer(Modifier.height(16.dp))

        Text(
            "Unlock CardVault",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Enter your 4-digit PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(32.dp))

        PinDotsRow(
            filledCount = ui.enteredPin.length,
            shakeError = ui.shakeError,
            onShakeComplete = mode.onShakeComplete,
        )

        ErrorLabel(ui.errorMessage)

        Spacer(Modifier.weight(0.06f))

        PhysicsKeypad(
            onDigitClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); mode.onDigitEntered(it) },
            onDeleteClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); mode.onDeleteDigit() },
            showBiometric = ui.canUseBiometric,
            onBiometricClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); mode.onBiometricClick() },
        )

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = mode.onForgotPinClick) {
            Text("Forgot PIN?", color = WalletPurple.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.weight(0.08f))
    }

    // ── Dialogs ────────────────────────────────────────────────────────
    if (ui.showRecoveryInput) {
        RecoveryCodeDialog(
            code = ui.recoveryCodeInput,
            error = ui.recoveryError,
            onCodeChanged = mode.onRecoveryCodeChanged,
            onVerify = mode.onVerifyRecoveryCode,
            onDismiss = mode.onHideRecovery,
            onDeleteAllData = mode.onShowWipeConfirmation,
        )
    }
    ConfirmationDialog(
        isVisible = ui.showWipeConfirmation,
        title = "Delete All Data?",
        message = "This will permanently erase all cards, categories, and settings. This action cannot be undone.",
        onConfirm = mode.onConfirmWipe,
        onDismiss = mode.onHideWipeConfirmation,
        type = ConfirmationType.DELETE,
        confirmText = "Delete All",
        dismissText = "Cancel",
    )
}

// ═══════════════════════════════════════════════════════════════════════
// SetupContent — create / change PIN (local state, same visuals)
//                Step 0 → Enter new PIN
//                Step 1 → Confirm PIN
//                On mismatch → shake + reset back to step 0
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SetupContent(
    mode: PinScreenMode.Setup,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    var step by remember { mutableIntStateOf(0) }
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shakeError by remember { mutableStateOf(false) }

    fun handleDigit(digit: Char) {
        if (currentPin.length >= 4) return
        errorMessage = null
        currentPin += digit
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        if (currentPin.length == 4) {
            if (step == 0) {
                // Save first entry, advance to confirm
                firstPin = currentPin
                currentPin = ""
                step = 1
            } else {
                // Confirm step
                if (currentPin == firstPin) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    mode.onPinConfirmed(currentPin)
                } else {
                    // Mismatch → shake, then reset everything to fresh entry
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    errorMessage = "PINs didn't match — try again"
                    shakeError = true
                    currentPin = ""
                    firstPin = ""
                    step = 0
                }
            }
        }
    }

    fun handleDelete() {
        if (currentPin.isEmpty()) return
        errorMessage = null
        currentPin = currentPin.dropLast(1)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
    ) {
        // ── Top bar with cancel ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = mode.onCancel) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel", tint = Color.White)
            }
        }

        Spacer(Modifier.weight(0.06f))

        // ── Step indicator ─────────────────────────────────────────────
        SetupStepIndicator(currentStep = step)

        Spacer(Modifier.height(24.dp))

        // ── Icon (same breathing animation, different icon per step) ──
        BreathingLockIcon(icon = if (step == 0) Icons.Default.Pin else Icons.Default.CheckCircle)

        Spacer(Modifier.height(16.dp))

        // ── Title with animated crossfade between steps ────────────────
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { -it / 4 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { it / 4 })
            },
            label = "setup_title",
        ) { s ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (s == 0) "Enter New PIN" else "Confirm PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (s == 0) "Choose a 4-digit PIN to protect your data"
                    else "Re-enter your PIN to confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── PIN dots ───────────────────────────────────────────────────
        PinDotsRow(
            filledCount = currentPin.length,
            shakeError = shakeError,
            onShakeComplete = { shakeError = false },
        )

        ErrorLabel(errorMessage)

        Spacer(Modifier.weight(0.06f))

        // ── Same physics keypad (no biometric in setup mode) ───────────
        PhysicsKeypad(
            onDigitClick = ::handleDigit,
            onDeleteClick = ::handleDelete,
            showBiometric = false,
            onBiometricClick = {},
        )

        Spacer(Modifier.height(24.dp))

        // ── Cancel link ────────────────────────────────────────────────
        TextButton(onClick = mode.onCancel) {
            Text(
                "Cancel",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.weight(0.08f))
    }
}

// ═══════════════════════════════════════════════════════════════════════
// SetupStepIndicator — Enter → Confirm progress dots
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SetupStepIndicator(currentStep: Int) {
    val labels = listOf("Enter PIN", "Confirm PIN")
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        labels.forEachIndexed { index, label ->
            val isActive = index <= currentStep
            val dotScale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.7f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                label = "step_dot_$index",
            )
            val dotColor by animateColorAsState(
                targetValue = if (isActive) WalletPurple else Color.White.copy(alpha = 0.3f),
                animationSpec = tween(250),
                label = "step_color_$index",
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                    .background(dotColor, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) WalletPurple else Color.White.copy(alpha = 0.35f),
            )

            if (index < labels.lastIndex) {
                Spacer(Modifier.width(8.dp))
                val lineColor by animateColorAsState(
                    targetValue = if (currentStep > 0) WalletPurple else Color.White.copy(alpha = 0.2f),
                    animationSpec = tween(300),
                    label = "step_line_color",
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(lineColor, RoundedCornerShape(1.dp)),
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Shared visual building blocks
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ErrorLabel(message: String?) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = WalletSpring.snappy(),
        ),
        exit = fadeOut(tween(150)),
    ) {
        Text(
            text = message ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFF6B6B),
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun BreathingLockIcon(icon: ImageVector = Icons.Default.Lock) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "breath_scale",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow_alpha",
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(listOf(WalletPurple.copy(alpha = glowAlpha), Color.Transparent)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Brush.linearGradient(listOf(WalletPurple, WalletBlue)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

// ── PinDotsRow + PinDot ─────────────────────────────────────────────

@Composable
private fun PinDotsRow(
    filledCount: Int,
    shakeError: Boolean,
    onShakeComplete: () -> Unit,
    totalDots: Int = 4,
) {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeError) {
        if (shakeError) {
            repeat(3) {
                shakeOffset.animateTo(
                    if (it % 2 == 0) 18f else -18f,
                    spring(Spring.DampingRatioHighBouncy, Spring.StiffnessHigh),
                )
            }
            shakeOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
            onShakeComplete()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.graphicsLayer { translationX = shakeOffset.value },
    ) {
        repeat(totalDots) { index -> PinDot(filled = index < filledCount, index = index) }
    }
}

@Composable
private fun PinDot(filled: Boolean, index: Int) {
    val scale by animateFloatAsState(
        targetValue = if (filled) 1f else 0.65f,
        animationSpec = if (filled) WalletSpring.bouncy() else WalletSpring.snappy(),
        label = "dot_scale_$index",
    )
    val dotColor by animateColorAsState(
        targetValue = if (filled) WalletPurple else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(200), label = "dot_color_$index",
    )
    val borderColor by animateColorAsState(
        targetValue = if (filled) WalletBlue else Color.White.copy(alpha = 0.3f),
        animationSpec = tween(200), label = "dot_border_$index",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (filled) 0.4f else 0f,
        animationSpec = tween(300), label = "dot_glow_$index",
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(28.dp)
                .scale(scale * 1.3f)
                .background(WalletPurple.copy(alpha = glowAlpha), CircleShape),
        )
        Box(
            Modifier
                .size(18.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(dotColor)
                .border(1.5.dp, borderColor, CircleShape),
        )
    }
}

// ── PhysicsKeypad ───────────────────────────────────────────────────

@Composable
private fun PhysicsKeypad(
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    showBiometric: Boolean,
    onBiometricClick: () -> Unit,
) {
    val keys = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        keys.forEachIndexed { ri, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                row.forEachIndexed { ci, digit ->
                    KeypadButton(digit.toString(), { onDigitClick(digit) }, (ri * 3 + ci) * WalletTiming.STAGGER_DELAY_MS)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            if (showBiometric) {
                KeypadIconButton(Icons.Default.Fingerprint, "Use biometrics", onBiometricClick, 9 * WalletTiming.STAGGER_DELAY_MS, accentColor = WalletBlue)
            } else {
                Spacer(Modifier.size(72.dp))
            }
            KeypadButton("0", { onDigitClick('0') }, 10 * WalletTiming.STAGGER_DELAY_MS)
            KeypadIconButton(Icons.AutoMirrored.Filled.Backspace, "Delete", onDeleteClick, 11 * WalletTiming.STAGGER_DELAY_MS)
        }
    }
}

// ── KeypadButton ────────────────────────────────────────────────────

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit, staggerDelayMs: Int, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        if (pressed) 0.82f else 1f,
        if (pressed) WalletSpring.snappy() else WalletSpring.bouncy(),
        label = "key_press_$label",
    )
    val surfaceAlpha by animateFloatAsState(if (pressed) 0.22f else 0.10f, tween(100), label = "key_alpha_$label")

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(staggerDelayMs.toLong()); visible = true }
    val entrance by animateFloatAsState(if (visible) 1f else 0f, WalletSpring.bouncy(), label = "key_entrance_$label")

    Box(
        modifier = modifier
            .size(72.dp)
            .graphicsLayer { scaleX = entrance * pressScale; scaleY = entrance * pressScale }
            .clip(CircleShape)
            .background(Color.White.copy(alpha = surfaceAlpha), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ── KeypadIconButton ────────────────────────────────────────────────

@Composable
private fun KeypadIconButton(
    icon: ImageVector, contentDescription: String, onClick: () -> Unit,
    staggerDelayMs: Int, modifier: Modifier = Modifier, accentColor: Color = Color.White,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(if (pressed) 0.82f else 1f, if (pressed) WalletSpring.snappy() else WalletSpring.bouncy(), label = "icon_press")
    val surfaceAlpha by animateFloatAsState(if (pressed) 0.18f else 0.06f, tween(100), label = "icon_alpha")

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(staggerDelayMs.toLong()); visible = true }
    val entrance by animateFloatAsState(if (visible) 1f else 0f, WalletSpring.bouncy(), label = "icon_entrance")

    Box(
        modifier = modifier
            .size(72.dp)
            .graphicsLayer { scaleX = entrance * pressScale; scaleY = entrance * pressScale }
            .clip(CircleShape)
            .background(accentColor.copy(alpha = surfaceAlpha), CircleShape)
            .border(1.dp, accentColor.copy(alpha = 0.15f), CircleShape)
            .clickable(interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = accentColor.copy(alpha = 0.9f), modifier = Modifier.size(26.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════════
// RecoveryCodeDialog — Forgot-PIN flow (unlock mode only)
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RecoveryCodeDialog(
    code: String, error: String?,
    onCodeChanged: (String) -> Unit, onVerify: () -> Unit,
    onDismiss: () -> Unit, onDeleteAllData: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Enter Recovery Code", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Enter the backup code you saved when setting up your PIN.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = code, onValueChange = onCodeChanged, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Recovery Code") }, placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
                    singleLine = true, isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onVerify, enabled = code.replace("-", "").replace(" ", "").length >= 12, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("Verify Code", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDeleteAllData) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete All Data", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
