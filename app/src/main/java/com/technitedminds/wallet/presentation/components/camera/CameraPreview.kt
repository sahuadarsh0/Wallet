package com.technitedminds.wallet.presentation.components.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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

            // Build resolution selector for the chosen aspect ratio
            val resolutionSelector = getResolutionSelector(aspectRatio)

            // Build preview
            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()

            // Build image capture
            val capture = ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
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
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Camera Error",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = initializationError!!,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                // Reset state and try again
                                initializationError = null
                                isCameraReady = false
                                // The LaunchedEffect will re-run
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                } else {
                    // Show loading state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Starting camera...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
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
 * Builds a [ResolutionSelector] for the given [CardAspectRatio].
 */
private fun getResolutionSelector(cardAspectRatio: CardAspectRatio): ResolutionSelector {
    val strategy = when (cardAspectRatio) {
        CardAspectRatio.RATIO_16_9 -> AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        else -> AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY // Default for card scanning
    }
    return ResolutionSelector.Builder()
        .setAspectRatioStrategy(strategy)
        .build()
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
 * Crops the captured image to match the overlay bounds.
 *
 * Important: CameraX writes captured JPEGs with the **sensor pixel orientation**
 * (typically landscape) and stores the rotation needed for upright display in
 * the EXIF `Orientation` tag. `BitmapFactory.decodeFile` does NOT honor that
 * tag, so [bitmap.width]/[bitmap.height] returned here would be the raw
 * sensor dimensions (e.g. 4032×3024 even when the user held the phone in
 * portrait).
 *
 * If we cropped against those raw dimensions, the resulting region would be
 * cut from the wrong axis (portrait target ratio applied to landscape pixels)
 * and the saved JPEG — which we strip of EXIF — would show up rotated 90°
 * relative to the live overlay.
 *
 * To fix this we:
 *   1. Read the JPEG's EXIF orientation.
 *   2. Rotate the decoded bitmap into its upright display orientation.
 *   3. Compute the crop against the upright dimensions, matching the overlay.
 *   4. Save the result as a new JPEG. The output has no EXIF rotation, but its
 *      pixel orientation matches what the user saw in the preview, so Coil and
 *      every other consumer renders it correctly.
 */
private fun cropImageToOverlayBounds(
    originalFile: File,
    aspectRatio: CardAspectRatio,
    context: Context
): File {
    val rawBitmap = android.graphics.BitmapFactory.decodeFile(originalFile.absolutePath)
        ?: throw IllegalStateException("Could not decode captured image: ${originalFile.absolutePath}")

    // Bring the bitmap into the orientation the user actually saw.
    val bitmap = rotateBitmapToUpright(rawBitmap, originalFile)

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

    val safeX = finalX.coerceAtLeast(0)
    val safeY = finalY.coerceAtLeast(0)
    val safeWidth = finalWidth.coerceAtMost(bitmap.width - safeX).coerceAtLeast(1)
    val safeHeight = finalHeight.coerceAtMost(bitmap.height - safeY).coerceAtLeast(1)

    val croppedBitmap = android.graphics.Bitmap.createBitmap(
        bitmap,
        safeX,
        safeY,
        safeWidth,
        safeHeight,
    )

    val croppedName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val croppedFile = File(context.cacheDir, "card_cropped_$croppedName.jpg")

    croppedFile.outputStream().use { out ->
        croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }

    // Recycle each distinct bitmap exactly once.
    // [croppedBitmap] may alias [bitmap] when the crop covers the full bitmap.
    // [bitmap] may alias [rawBitmap] when no EXIF rotation was needed.
    val distinctBitmaps = setOf(rawBitmap, bitmap, croppedBitmap)
    distinctBitmaps.forEach { if (!it.isRecycled) it.recycle() }

    return croppedFile
}

/**
 * Returns a bitmap rotated according to the EXIF orientation tag on [sourceFile].
 *
 * Falls back to the original bitmap if EXIF can't be read or no rotation is needed.
 * Mirroring (transverse / transpose) is also handled because some front-camera
 * captures encode it.
 */
private fun rotateBitmapToUpright(
    bitmap: android.graphics.Bitmap,
    sourceFile: File,
): android.graphics.Bitmap {
    val orientation = try {
        android.media.ExifInterface(sourceFile.absolutePath)
            .getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL,
            )
    } catch (e: Exception) {
        Log.w("CameraPreview", "Failed to read EXIF orientation: ${e.message}")
        android.media.ExifInterface.ORIENTATION_NORMAL
    }

    val matrix = android.graphics.Matrix()
    when (orientation) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 ->
            matrix.postRotate(90f)
        android.media.ExifInterface.ORIENTATION_ROTATE_180 ->
            matrix.postRotate(180f)
        android.media.ExifInterface.ORIENTATION_ROTATE_270 ->
            matrix.postRotate(270f)
        android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
            matrix.postScale(-1f, 1f)
        android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL ->
            matrix.postScale(1f, -1f)
        android.media.ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f); matrix.postScale(-1f, 1f)
        }
        android.media.ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f); matrix.postScale(-1f, 1f)
        }
        else -> return bitmap
    }

    return try {
        android.graphics.Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true,
        )
    } catch (oom: OutOfMemoryError) {
        Log.w("CameraPreview", "OOM rotating bitmap; using raw orientation")
        bitmap
    }
}
