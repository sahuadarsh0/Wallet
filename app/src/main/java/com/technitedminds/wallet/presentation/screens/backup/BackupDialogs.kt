package com.technitedminds.wallet.presentation.screens.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.backup.RestoreConflictStrategy
import com.technitedminds.wallet.domain.model.backup.RestorePreview
import com.technitedminds.wallet.domain.repository.BackupProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Asks the user for a passphrase to encrypt a new backup. Requires the user
 * to type the passphrase twice (with strength hint) before enabling Continue.
 */
@Composable
fun ExportPassphraseDialog(
    onConfirm: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    var pass1 by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    val mismatch = pass2.isNotEmpty() && pass1 != pass2
    val tooShort = pass1.isNotEmpty() && pass1.length < 8
    val canContinue = !mismatch && !tooShort && pass1.length >= 8 && pass1 == pass2

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Encrypt this backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Choose a strong passphrase. You'll need this exact passphrase to restore the backup. " +
                        "We can't recover it for you — there's nothing on a server.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = pass1,
                    onValueChange = { pass1 = it },
                    label = { Text("Passphrase") },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = tooShort,
                    supportingText = if (tooShort) {
                        { Text("Use at least 8 characters.") }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Hide passphrase" else "Show passphrase",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pass2,
                    onValueChange = { pass2 = it },
                    label = { Text("Confirm passphrase") },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = mismatch,
                    supportingText = if (mismatch) {
                        { Text("Passphrases don't match.") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pass1.toCharArray()) },
                enabled = canContinue,
            ) {
                Text("Encrypt & save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Asks the user for the passphrase that protects an existing backup file.
 */
@Composable
fun RestorePassphraseDialog(
    onConfirm: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    var pass by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Enter backup passphrase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter the passphrase that was set when this backup was created.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Passphrase") },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Hide passphrase" else "Show passphrase",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pass.toCharArray()) },
                enabled = pass.isNotEmpty(),
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Surfaces the manifest summary + a conflict-strategy selector. The user
 * can choose how duplicates should behave before any DB writes happen.
 */
@Composable
fun RestorePreviewDialog(
    preview: RestorePreview,
    onConfirm: (RestoreConflictStrategy) -> Unit,
    onDismiss: () -> Unit,
) {
    var strategy by remember {
        mutableStateOf(
            if (preview.conflictingCardCount > 0) RestoreConflictStrategy.SKIP_EXISTING
            else RestoreConflictStrategy.KEEP_BOTH,
        )
    }
    val createdAt = remember(preview.createdAtEpochMs) {
        SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())
            .format(Date(preview.createdAtEpochMs))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Restore from backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PreviewLine(label = "Created", value = createdAt)
                    PreviewLine(label = "From CardVault", value = "v${preview.sourceAppVersion}")
                    PreviewLine(label = "Cards", value = preview.cardCount.toString())
                    PreviewLine(label = "Categories", value = preview.categoryCount.toString())
                    PreviewLine(label = "Card images", value = preview.imageCount.toString())
                    if (preview.conflictingCardCount > 0) {
                        PreviewLine(
                            label = "Already on this device",
                            value = "${preview.conflictingCardCount} card${if (preview.conflictingCardCount == 1) "" else "s"}",
                            highlight = true,
                        )
                    }
                }

                Text(
                    text = if (preview.conflictingCardCount > 0)
                        "How should we handle the duplicates?"
                    else
                        "How should we handle duplicates if any are found?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                RestoreConflictStrategy.entries.forEach { option ->
                    StrategyOption(
                        option = option,
                        isSelected = strategy == option,
                        onClick = { strategy = option },
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(strategy) }) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun PreviewLine(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StrategyOption(
    option: RestoreConflictStrategy,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else Color.Transparent,
            )
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Column(modifier = Modifier.padding(top = 6.dp)) {
            Text(
                text = option.displayTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) border else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = option.displayDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Lightweight indeterminate progress dialog shown during multi-second
 * encryption, decryption, or restore phases.
 */
@Composable
fun BackupProgressDialog(
    title: String,
    phase: BackupProgress?,
    onCancel: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { /* not dismissable while busy */ },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 4.dp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phaseToText(phase),
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            if (onCancel != null) {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        },
    )
}

private fun phaseToText(phase: BackupProgress?): String = when (phase) {
    null -> "Working…"
    BackupProgress.GATHERING_DATA -> "Reading your wallet…"
    BackupProgress.PACKAGING -> "Packaging cards and images…"
    BackupProgress.ENCRYPTING -> "Encrypting…"
    BackupProgress.WRITING -> "Saving to Downloads/CardVault…"
    BackupProgress.READING -> "Opening backup file…"
    BackupProgress.DECRYPTING -> "Decrypting…"
    BackupProgress.UNPACKING -> "Reading backup contents…"
    BackupProgress.APPLYING -> "Restoring to your wallet…"
    BackupProgress.DONE -> "Done."
}
