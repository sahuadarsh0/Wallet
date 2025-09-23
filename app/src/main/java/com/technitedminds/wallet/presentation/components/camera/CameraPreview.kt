package com.technitedminds.wallet.presentation.components.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Camera preview component for card scanning.
 * This is a simplified implementation that provides the interface
 * for camera functionality without external dependencies.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (File) -> Unit = {},
    onError: (Exception) -> Unit = {},
    aspectRatio: CardAspectRatio = CardAspectRatio.CREDIT_CARD
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Camera preview placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(aspectRatio.ratio),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Preview",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Camera functionality requires additional dependencies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // Simulate image capture for testing
                        try {
                            val tempFile = File(context.cacheDir, "test_capture.jpg")
                            onImageCaptured(tempFile)
                        } catch (e: Exception) {
                            onError(e)
                        }
                    }
                ) {
                    Text("Simulate Capture")
                }
            }
        }
        
        // Overlay the card positioning guide
        CardOverlay(
            aspectRatio = aspectRatio,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Camera configuration for optimal card scanning
 */
object CameraConfig {
    const val ASPECT_RATIO_16_9 = 16f / 9f
    const val ASPECT_RATIO_4_3 = 4f / 3f
    const val CREDIT_CARD_RATIO = 1.586f

    /** Gets the optimal aspect ratio for card scanning */
    fun getOptimalAspectRatio(): Float = CREDIT_CARD_RATIO

    /** Camera quality settings */
    enum class Quality {
        LOW, MEDIUM, HIGH, MAX
    }
    
    /** Flash modes */
    enum class FlashMode {
        OFF, ON, AUTO
    }
}

/**
 * Camera capture result
 */
sealed class CameraCaptureResult {
    data class Success(val imageFile: File) : CameraCaptureResult()
    data class Error(val exception: Exception) : CameraCaptureResult()
}

/**
 * Camera state management
 */
data class CameraState(
    val isInitialized: Boolean = false,
    val isCapturing: Boolean = false,
    val flashMode: CameraConfig.FlashMode = CameraConfig.FlashMode.OFF,
    val quality: CameraConfig.Quality = CameraConfig.Quality.HIGH,
    val error: String? = null
)

/**
 * Camera permission utilities
 */
object CameraPermissionUtils {
    
    /** Checks if camera permission is granted */
    fun isCameraPermissionGranted(context: android.content.Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /** Opens app settings for manual permission grant */
    fun openAppSettings(context: android.content.Context) {
        val intent = android.content.Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}