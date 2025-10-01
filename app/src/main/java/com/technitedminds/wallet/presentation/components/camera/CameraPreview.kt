package com.technitedminds.wallet.presentation.components.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Camera preview component for card scanning using CameraX.
 * Provides live camera preview with optimized settings for card capture.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (File) -> Unit = {},
    onError: (Exception) -> Unit = {},
    aspectRatio: CardAspectRatio = CardAspectRatio.CREDIT_CARD,
    flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder()
                            .setTargetAspectRatio(getAspectRatio(aspectRatio))
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        
                        imageCapture = ImageCapture.Builder()
                            .setTargetAspectRatio(getAspectRatio(aspectRatio))
                            .setFlashMode(flashMode)
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        
                        cameraProvider?.unbindAll()
                        
                        camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                        onError(exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
        
        // Overlay the card positioning guide
        CardOverlay(
            aspectRatio = aspectRatio,
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // Expose capture function
    LaunchedEffect(imageCapture) {
        // This effect allows external components to trigger capture
        // The actual capture will be triggered by the CaptureButton
    }
}

/**
 * Enhanced camera preview with capture functionality
 */
@Composable
fun CameraPreviewWithCapture(
    modifier: Modifier = Modifier,
    onImageCaptured: (File) -> Unit,
    onError: (Exception) -> Unit,
    aspectRatio: CardAspectRatio = CardAspectRatio.CREDIT_CARD,
    flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    captureButtonState: CaptureButtonState = CaptureButtonState.IDLE,
    onCaptureClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                setupCamera(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    aspectRatio = aspectRatio,
                    flashMode = flashMode,
                    lensFacing = lensFacing,
                    onCameraReady = { provider, capture, cam ->
                        cameraProvider = provider
                        imageCapture = capture
                        camera = cam
                    },
                    onError = onError
                )
            }
        )
        
        // Card overlay
        CardOverlay(
            aspectRatio = aspectRatio,
            isCapturing = captureButtonState == CaptureButtonState.CAPTURING,
            modifier = Modifier.fillMaxSize()
        )
        
        // Camera controls
        CameraControls(
            onCaptureClick = {
                imageCapture?.let { capture ->
                    captureImage(
                        imageCapture = capture,
                        context = context,
                        executor = cameraExecutor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                }
                onCaptureClick()
            },
            onFlashToggle = {
                // Flash toggle logic would be implemented here
                // This requires rebuilding the ImageCapture use case
            },
            onSwitchCamera = {
                // Camera switching logic would be implemented here
                // This requires rebuilding all use cases with different lens facing
            },
            captureButtonState = captureButtonState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Sets up the camera with proper configuration
 */
private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    aspectRatio: CardAspectRatio,
    flashMode: Int,
    lensFacing: Int,
    onCameraReady: (ProcessCameraProvider, ImageCapture, Camera) -> Unit,
    onError: (Exception) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .setTargetAspectRatio(getAspectRatio(aspectRatio))
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            val imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(getAspectRatio(aspectRatio))
                .setFlashMode(flashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            
            cameraProvider.unbindAll()
            
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            onCameraReady(cameraProvider, imageCapture, camera)
            
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
            onError(exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

/**
 * Captures an image using the provided ImageCapture use case
 */
private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    executor: ExecutorService,
    onImageCaptured: (File) -> Unit,
    onError: (Exception) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val photoFile = File(context.cacheDir, "card_capture_$name.jpg")
    
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture.takePicture(
        outputFileOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                onError(exception)
            }
            
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraPreview", "Photo capture succeeded: ${photoFile.absolutePath}")
                onImageCaptured(photoFile)
            }
        }
    )
}

/**
 * Converts CardAspectRatio to CameraX AspectRatio
 */
private fun getAspectRatio(cardAspectRatio: CardAspectRatio): Int {
    return when (cardAspectRatio) {
        CardAspectRatio.RATIO_16_9 -> AspectRatio.RATIO_16_9
        CardAspectRatio.RATIO_4_3 -> AspectRatio.RATIO_4_3
        else -> AspectRatio.RATIO_4_3 // Default for card scanning
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
    val isUsingFrontCamera: Boolean = false,
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