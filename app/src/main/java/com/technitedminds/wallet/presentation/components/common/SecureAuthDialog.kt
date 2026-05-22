package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.technitedminds.wallet.data.local.security.AppLockRepository
import com.technitedminds.wallet.data.local.security.PinVerifyResult
import com.technitedminds.wallet.presentation.screens.security.BiometricAuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

/**
 * Hilt entry point used to obtain security primitives from a Composable
 * without requiring the host screen's ViewModel to depend on them.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SecureAuthEntryPoint {
    fun appLockRepository(): AppLockRepository
    fun biometricAuthManager(): BiometricAuthManager
}

/**
 * Reusable re-authentication dialog that gates a sensitive action
 * (e.g. copying decrypted card details) behind biometric or PIN verification.
 *
 * Visual style mirrors [ConfirmationDialog]: a rounded surface card with a
 * gradient icon medallion, scale-in animation, and the same primary/secondary
 * button treatment used everywhere else in the app.
 *
 * Auto-launches the system biometric prompt on first show when biometrics are
 * available, and falls back to a 4-digit PIN entry that goes through the same
 * rate-limiting pipeline as [AppLockRepository.verifyPin].
 *
 * @param isVisible Whether the dialog is shown.
 * @param title Title text shown in the dialog.
 * @param message Body text explaining why authentication is required.
 * @param onDismiss Called when the user cancels.
 * @param onAuthenticated Called once the user has successfully authenticated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureAuthDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    if (!isVisible) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecureAuthEntryPoint::class.java
        )
    }
    val appLockRepository = remember(entryPoint) { entryPoint.appLockRepository() }
    val biometricAuthManager = remember(entryPoint) { entryPoint.biometricAuthManager() }

    val activity = remember(context) {
        var current: android.content.Context? = context
        while (current is android.content.ContextWrapper && current !is FragmentActivity) {
            current = current.baseContext
        }
        current as? FragmentActivity
    }
    val biometricAvailable = remember(biometricAuthManager) {
        biometricAuthManager.isBiometricAvailable()
    }

    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var biometricAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible && !biometricAttempted && activity != null && biometricAvailable) {
            biometricAttempted = true
            biometricAuthManager.authenticate(
                activity = activity,
                onSuccess = { onAuthenticated() },
                onFailure = { msg ->
                    errorMessage = msg.takeIf { it.isNotBlank() }
                },
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isVerifying,
            dismissOnClickOutside = !isVerifying,
        ),
    ) {
        SecureAuthDialogContent(
            title = title,
            message = message,
            pin = pin,
            errorMessage = errorMessage,
            isVerifying = isVerifying,
            showBiometric = activity != null && biometricAvailable,
            onPinChange = { input ->
                if (input.length <= 4 && input.all { it.isDigit() }) {
                    pin = input
                    errorMessage = null
                }
            },
            onBiometricClick = {
                if (activity != null) {
                    biometricAuthManager.authenticate(
                        activity = activity,
                        onSuccess = { onAuthenticated() },
                        onFailure = { msg ->
                            errorMessage = msg.takeIf { it.isNotBlank() }
                        },
                    )
                }
            },
            onVerifyClick = {
                isVerifying = true
                scope.launch {
                    when (val result = appLockRepository.verifyPin(pin)) {
                        is PinVerifyResult.Success -> {
                            pin = ""
                            errorMessage = null
                            onAuthenticated()
                        }
                        is PinVerifyResult.Wrong -> {
                            pin = ""
                            errorMessage = if (result.attemptsRemaining > 0) {
                                "Incorrect PIN. ${result.attemptsRemaining} attempts left."
                            } else {
                                "Incorrect PIN."
                            }
                        }
                        is PinVerifyResult.LockedOut -> {
                            pin = ""
                            errorMessage = "Locked out. Try again in ${result.remainingSeconds}s."
                        }
                        is PinVerifyResult.WipeTriggered -> {
                            pin = ""
                            errorMessage = "Too many attempts."
                        }
                    }
                    isVerifying = false
                }
            },
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecureAuthDialogContent(
    title: String,
    message: String,
    pin: String,
    errorMessage: String?,
    isVerifying: Boolean,
    showBiometric: Boolean,
    onPinChange: (String) -> Unit,
    onBiometricClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "secure_auth_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "secure_auth_alpha",
    )

    val accent = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Gradient icon medallion (matches AnimatedSectionHeader)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(accent, accent.copy(alpha = 0.75f)),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = onPinChange,
                label = { Text("Enter PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = errorMessage != null,
                enabled = !isVerifying,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = slideInVertically { -it / 2 } + fadeIn(),
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            if (showBiometric) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onBiometricClick,
                    enabled = !isVerifying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = accent.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Use biometric",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isVerifying,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Button(
                    onClick = onVerifyClick,
                    enabled = pin.length == 4 && !isVerifying,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = if (isVerifying) "Verifying…" else "Verify",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
