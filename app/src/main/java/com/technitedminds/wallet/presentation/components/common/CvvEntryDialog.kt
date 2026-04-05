package com.technitedminds.wallet.presentation.components.common

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Dialog for manually entering CVV after a successful NFC card read.
 * NFC cannot extract the CVV so the user must provide it themselves.
 */
@Composable
fun CvvEntryDialog(
    cardScheme: String,
    maskedCardNumber: String,
    onCvvConfirmed: (String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var cvv by remember { mutableStateOf("") }
    var cvvError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* non-dismissible — user must confirm or skip */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_LARGE),
            tonalElevation = AppConstants.Dimensions.CARD_ELEVATION_SELECTED,
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // NFC success badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

                Text(
                    text = AppConstants.NfcText.CARD_READ_SUCCESS,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

                Text(
                    text = "$cardScheme $maskedCardNumber",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

                Text(
                    text = AppConstants.NfcText.CVV_PROMPT,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

                PremiumTextField(
                    value = cvv,
                    onValueChange = { newValue ->
                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            cvv = newValue
                            cvvError = null
                        }
                    },
                    label = AppConstants.UIText.CVV_LABEL,
                    leadingIcon = Icons.Default.Security,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = cvvError != null,
                    errorMessage = cvvError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

                Button(
                    onClick = {
                        if (cvv.length in 3..4) {
                            onCvvConfirmed(cvv)
                        } else {
                            cvvError = AppConstants.NfcText.CVV_VALIDATION_ERROR
                        }
                    },
                    enabled = cvv.length in 3..4,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppConstants.DialogText.CONFIRM_BUTTON)
                }

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppConstants.NfcText.SKIP_CVV)
                }
            }
        }
    }
}
