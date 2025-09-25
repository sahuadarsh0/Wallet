package com.technitedminds.wallet.presentation.components.camera

import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Sealed class representing different camera error states */
sealed class CameraError {
    object CameraNotAvailable : CameraError()
    object CameraInUse : CameraError()
    object CameraDisabled : CameraError()
    data class ImageCaptureError(val exception: ImageCaptureException) : CameraError()
    data class UnknownError(val exception: Exception) : CameraError()

    fun getMessage(): String =
            when (this) {
                is CameraNotAvailable -> "Camera is not available on this device"
                is CameraInUse -> "Camera is currently being used by another app"
                is CameraDisabled -> "Camera has been disabled"
                is ImageCaptureError -> "Failed to capture image: ${exception.message}"
                is UnknownError -> "An unexpected error occurred: ${exception.message}"
            }

    fun getTitle(): String =
            when (this) {
                is CameraNotAvailable -> "Camera Unavailable"
                is CameraInUse -> "Camera In Use"
                is CameraDisabled -> "Camera Disabled"
                is ImageCaptureError -> "Capture Failed"
                is UnknownError -> "Camera Error"
            }
}

/** Composable that displays camera error states with appropriate UI */
@Composable
fun CameraErrorContent(
        error: CameraError,
        onRetry: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Card(
                modifier = Modifier.padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = error.getTitle(),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = error.getMessage(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (error) {
                    is CameraError.CameraNotAvailable, is CameraError.CameraDisabled -> {
                        Button(onClick = onDismiss) { Text("Go Back") }
                    }
                    else -> {
                        Button(onClick = onRetry) { Text("Try Again") }
                    }
                }
            }
        }
    }
}

/** Utility functions for camera error handling */
object CameraErrorHandler {

    /** Maps exceptions to CameraError types */
    fun mapException(exception: Exception): CameraError {
        return when (exception) {
            is ImageCaptureException -> CameraError.ImageCaptureError(exception)
            else -> {
                // Try to determine error type from message
                val message = exception.message?.lowercase() ?: ""
                when {
                    message.contains("camera") && message.contains("use") -> CameraError.CameraInUse
                    message.contains("camera") && message.contains("available") ->
                            CameraError.CameraNotAvailable
                    message.contains("camera") && message.contains("disabled") ->
                            CameraError.CameraDisabled
                    else -> CameraError.UnknownError(exception)
                }
            }
        }
    }

    /** Checks if the error is recoverable (user can retry) */
    fun isRecoverable(error: CameraError): Boolean {
        return when (error) {
            is CameraError.CameraNotAvailable, is CameraError.CameraDisabled -> false
            else -> true
        }
    }

    /** Gets user-friendly error message with suggestions */
    fun getErrorMessageWithSuggestion(error: CameraError): String {
        val baseMessage = error.getMessage()
        val suggestion =
                when (error) {
                    is CameraError.CameraInUse -> "\n\nTry closing other camera apps and try again."
                    is CameraError.CameraNotAvailable ->
                            "\n\nThis device may not have a camera or it's not accessible."
                    is CameraError.CameraDisabled ->
                            "\n\nCheck your device settings to enable camera access."
                    is CameraError.ImageCaptureError ->
                            "\n\nMake sure you have enough storage space and try again."
                    is CameraError.UnknownError -> "\n\nRestart the app or try again later."
                }
        return baseMessage + suggestion
    }
}
