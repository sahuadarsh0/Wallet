package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.presentation.screens.addcard.NfcReadState

/**
 * Full-screen dialog overlay for NFC card scanning.
 * Shows a pulsing contactless icon during scanning, a check on success,
 * or an error state with retry.
 */
@Composable
fun NfcScanningSheet(
    nfcReadState: NfcReadState,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_LARGE),
            tonalElevation = AppConstants.Dimensions.CARD_ELEVATION_SELECTED,
            modifier = modifier.fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = nfcReadState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "nfc_state",
            ) { state ->
                when (state) {
                    is NfcReadState.Scanning -> ScanningContent(onCancel = onCancel)
                    is NfcReadState.Error -> ErrorContent(
                        message = state.message,
                        onRetry = onRetry,
                        onCancel = onCancel,
                    )
                    is NfcReadState.Success -> SuccessContent(
                        cardScheme = state.data.cardScheme,
                        maskedNumber = state.data.maskedCardNumber(),
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ScanningContent(onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc_scale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc_alpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .alpha(alpha)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Contactless,
                contentDescription = "NFC scanning",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Text(
            text = AppConstants.NfcText.SCANNING_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

        Text(
            text = AppConstants.NfcText.SCANNING_SUBTITLE,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppConstants.DialogText.CANCEL_BUTTON)
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = AppConstants.UIText.ERROR_ICON,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Text(
            text = AppConstants.NfcText.READ_FAILED_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppConstants.UIText.TRY_AGAIN)
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppConstants.DialogText.CANCEL_BUTTON)
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))
    }
}

@Composable
private fun SuccessContent(
    cardScheme: String,
    maskedNumber: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Text(
            text = AppConstants.NfcText.READ_SUCCESS_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

        Text(
            text = "$cardScheme $maskedNumber",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))
    }
}
