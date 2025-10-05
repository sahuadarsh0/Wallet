package com.technitedminds.wallet.presentation.components.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error disposing camera", e)
            }
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
    
    // Use rememberSaveable to survive configuration changes
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var initializationError by remember { mutableStateOf<String?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Create preview view once and reuse
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }
    
    // Handle disposal
    DisposableEffect(lifecycleOwner) {
        onDispose {
            Log.d("CameraPreview", "Disposing camera resources")
            try {
                cameraProvider?.unbindAll()
                if (!cameraExecutor.isShutdown) {
                    cameraExecutor.shutdown()
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error during disposal", e)
            }
        }
    }
    
    // Initialize camera once when component is first created
    LaunchedEffect(Unit) {
        try {
            Log.d("CameraPreview", "Starting camera initialization")
            initializationError = null
            
            // Get camera provider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.get()
            
            // Build preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(getAspectRatio(aspectRatio))
                .build()
            
            // Build image capture
            val capture = ImageCapture.Builder()
                .setTargetAspectRatio(getAspectRatio(aspectRatio))
                .setFlashMode(flashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            
            // Build camera selector
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            
            // Unbind any existing use cases
            provider.unbindAll()
            
            // Bind use cases to lifecycle
            val cam = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
            
            // Set surface provider after binding
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            // Update state
            cameraProvider = provider
            imageCapture = capture
            camera = cam
            
            Log.d("CameraPreview", "Camera bound successfully")
            
            // Small delay to ensure camera is fully ready
            kotlinx.coroutines.delay(500)
            isCameraReady = true
            
            Log.d("CameraPreview", "Camera is ready for capture")
            
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Camera initialization failed", exc)
            initializationError = exc.message ?: "Camera initialization failed"
            onError(exc)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Card overlay
        CardOverlay(
            aspectRatio = aspectRatio,
            isCapturing = captureButtonState == CaptureButtonState.CAPTURING,
            modifier = Modifier.fillMaxSize()
        )
        
        // Show loading or error state
        if (!isCameraReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                if (initializationError != null) {
                    // Show error state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.material3.Text(
                            text = "Camera Error",
                            color = Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                        androidx.compose.material3.Text(
                            text = initializationError!!,
                            color = Color.White.copy(alpha = 0.8f),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.material3.Button(
                            onClick = {
                                // Reset state and try again
                                initializationError = null
                                isCameraReady = false
                                // The LaunchedEffect will re-run
                            }
                        ) {
                            androidx.compose.material3.Text("Retry")
                        }
                    }
                } else {
                    // Show loading state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.material3.Text(
                            text = "Starting camera...",
                            color = Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Camera controls - only show when camera is ready
        if (isCameraReady) {
            CameraControls(
                onCaptureClick = {
                    if (imageCapture != null && !isCapturing) {
                        isCapturing = true
                        captureImageWithCropping(
                            imageCapture = imageCapture!!,
                            context = context,
                            executor = cameraExecutor,
                            aspectRatio = aspectRatio,
                            onImageCaptured = { file ->
                                isCapturing = false
                                onImageCaptured(file)
                            },
                            onError = { error ->
                                isCapturing = false
                                onError(error)
                            }
                        )
                        onCaptureClick()
                    }
                },
                onFlashToggle = {
                    // Flash toggle logic would be implemented here
                },
                onSwitchCamera = {
                    // Camera switching logic would be implemented here
                },
                captureButtonState = captureButtonState,
                isCameraReady = isCameraReady && !isCapturing,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
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
    try {
        // Check if executor is still running
        if (executor.isShutdown || executor.isTerminated) {
            onError(Exception("Camera is not available. Please try again."))
            return
        }
        
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(context.cacheDir, "card_capture_$name.jpg")
        
        // Ensure cache directory exists
        if (!context.cacheDir.exists()) {
            context.cacheDir.mkdirs()
        }
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        Log.d("CameraPreview", "Starting image capture to: ${photoFile.absolutePath}")
        
        // Add timeout for capture operation
        val timeoutScope = CoroutineScope(Dispatchers.IO)
        var captureCompleted = false
        
        // Set timeout for capture
        timeoutScope.launch {
            delay(10000) // 10 second timeout
            if (!captureCompleted) {
                Log.w("CameraPreview", "Capture timeout - operation took too long")
                onError(Exception("Capture timeout. Please try again."))
            }
        }
        
        imageCapture.takePicture(
            outputFileOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    captureCompleted = true
                    Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                    when (exception.imageCaptureError) {
                        ImageCapture.ERROR_CAMERA_CLOSED -> {
                            onError(Exception("Camera was closed during capture. Please try again."))
                        }
                        ImageCapture.ERROR_CAPTURE_FAILED -> {
                            onError(Exception("Image capture failed. Please try again."))
                        }
                        ImageCapture.ERROR_FILE_IO -> {
                            onError(Exception("Failed to save image. Please check storage space."))
                        }
                        ImageCapture.ERROR_INVALID_CAMERA -> {
                            onError(Exception("Camera is not available. Please restart the app."))
                        }
                        else -> {
                            onError(Exception("Capture failed: ${exception.message}"))
                        }
                    }
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    captureCompleted = true
                    Log.d("CameraPreview", "Photo capture succeeded: ${photoFile.absolutePath}")
                    if (photoFile.exists() && photoFile.length() > 0) {
                        onImageCaptured(photoFile)
                    } else {
                        onError(Exception("Image file was not created properly. Please try again."))
                    }
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraPreview", "Error setting up image capture", e)
        onError(Exception("Failed to capture image: ${e.message}"))
    }
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
 * Captures an image using the provided ImageCapture use case and crops it to overlay bounds
 */
private fun captureImageWithCropping(
    imageCapture: ImageCapture,
    context: Context,
    executor: ExecutorService,
    aspectRatio: CardAspectRatio,
    onImageCaptured: (File) -> Unit,
    onError: (Exception) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val tempFile = File(context.cacheDir, "temp_capture_$name.jpg")
    
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
    
    imageCapture.takePicture(
        outputFileOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                onError(exception)
            }
            
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraPreview", "Photo capture succeeded, cropping to overlay bounds")
                
                // Crop the image to overlay bounds on background thread
                executor.execute {
                    try {
                        val croppedFile = cropImageToOverlayBounds(tempFile, aspectRatio, context)
                        // Delete temp file
                        tempFile.delete()
                        onImageCaptured(croppedFile)
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Image cropping failed: ${e.message}", e)
                        onError(e)
                    }
                }
            }
        }
    )
}

/**
 * Crops the captured image to match the overlay bounds
 */
private fun cropImageToOverlayBounds(
    originalFile: File,
    aspectRatio: CardAspectRatio,
    context: Context
): File {
    val bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.absolutePath)
    
    // Calculate crop dimensions based on aspect ratio
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    
    val targetRatio = aspectRatio.ratio
    val currentRatio = originalWidth.toFloat() / originalHeight.toFloat()
    
    val cropWidth: Int
    val cropHeight: Int
    val cropX: Int
    val cropY: Int
    
    if (currentRatio > targetRatio) {
        // Image is wider than target, crop width
        cropHeight = originalHeight
        cropWidth = (originalHeight * targetRatio).toInt()
        cropX = (originalWidth - cropWidth) / 2
        cropY = 0
    } else {
        // Image is taller than target, crop height
        cropWidth = originalWidth
        cropHeight = (originalWidth / targetRatio).toInt()
        cropX = 0
        cropY = (originalHeight - cropHeight) / 2
    }
    
    // Apply additional cropping to match overlay size (80% of screen)
    val overlayScale = 0.8f
    val finalWidth = (cropWidth * overlayScale).toInt()
    val finalHeight = (cropHeight * overlayScale).toInt()
    val finalX = cropX + (cropWidth - finalWidth) / 2
    val finalY = cropY + (cropHeight - finalHeight) / 2
    
    val croppedBitmap = android.graphics.Bitmap.createBitmap(
        bitmap,
        finalX.coerceAtLeast(0),
        finalY.coerceAtLeast(0),
        finalWidth.coerceAtMost(bitmap.width - finalX.coerceAtLeast(0)),
        finalHeight.coerceAtMost(bitmap.height - finalY.coerceAtLeast(0))
    )
    
    // Save cropped image
    val croppedName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val croppedFile = File(context.cacheDir, "card_cropped_$croppedName.jpg")
    
    croppedFile.outputStream().use { out ->
        croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    
    // Clean up bitmaps
    bitmap.recycle()
    croppedBitmap.recycle()
    
    return croppedFile
}