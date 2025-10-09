package com.technitedminds.wallet.presentation.components.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages camera operations for card scanning with CameraX.
 * Provides a clean interface for camera setup, configuration, and image capture.
 */
@Singleton
class CameraManager @Inject constructor() {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService? = null
    
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    /**
     * Initializes the camera with the given configuration
     */
    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        config: CameraConfiguration = CameraConfiguration()
    ): Result<Unit> {
        return try {
            cameraExecutor = Executors.newSingleThreadExecutor()
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.get()
            
            setupCameraUseCases(
                provider = provider,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                config = config
            )
            
            cameraProvider = provider
            _cameraState.value = _cameraState.value.copy(
                isInitialized = true,
                error = null
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CameraManager", "Camera initialization failed", e)
            _cameraState.value = _cameraState.value.copy(
                isInitialized = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    /**
     * Captures an image with the current camera configuration
     */
    suspend fun captureImage(context: Context): Result<File> {
        return try {
            val capture = imageCapture ?: return Result.failure(
                IllegalStateException("Camera not initialized")
            )
            
            _cameraState.value = _cameraState.value.copy(isCapturing = true)
            
            val photoFile = createImageFile(context)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            
            val result = kotlin.coroutines.suspendCoroutine<Result<File>> { continuation ->
                capture.takePicture(
                    outputFileOptions,
                    cameraExecutor ?: ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraManager", "Photo capture failed", exception)
                            continuation.resume(Result.failure(exception))
                        }
                        
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("CameraManager", "Photo saved: ${photoFile.absolutePath}")
                            continuation.resume(Result.success(photoFile))
                        }
                    }
                )
            }
            
            _cameraState.value = _cameraState.value.copy(isCapturing = false)
            result
            
        } catch (e: Exception) {
            _cameraState.value = _cameraState.value.copy(
                isCapturing = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    /**
     * Toggles the camera flash mode
     */
    fun toggleFlash() {
        val currentFlashMode = _cameraState.value.flashMode
        val newFlashMode = when (currentFlashMode) {
            CameraConfig.FlashMode.OFF -> CameraConfig.FlashMode.ON
            CameraConfig.FlashMode.ON -> CameraConfig.FlashMode.AUTO
            CameraConfig.FlashMode.AUTO -> CameraConfig.FlashMode.OFF
        }
        
        _cameraState.value = _cameraState.value.copy(flashMode = newFlashMode)
        
        // Update ImageCapture flash mode
        imageCapture?.flashMode = when (newFlashMode) {
            CameraConfig.FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            CameraConfig.FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            CameraConfig.FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
        }
    }
    
    /**
     * Switches between front and back camera
     */
    suspend fun switchCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Result<Unit> {
        return try {
            val currentLensFacing = if (_cameraState.value.isUsingFrontCamera) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            
            val config = CameraConfiguration(lensFacing = currentLensFacing)
            
            cameraProvider?.let { provider ->
                setupCameraUseCases(
                    provider = provider,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    config = config
                )
            }
            
            _cameraState.value = _cameraState.value.copy(
                isUsingFrontCamera = !_cameraState.value.isUsingFrontCamera
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CameraManager", "Camera switch failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Releases camera resources
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        
        cameraProvider = null
        imageCapture = null
        camera = null
        cameraExecutor = null
        
        _cameraState.value = CameraState()
    }
    
    /**
     * Checks if the device has a front camera
     */
    suspend fun hasFrontCamera(context: Context): Boolean {
        return try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.get()
            provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets available camera capabilities
     */
    suspend fun getCameraCapabilities(context: Context): CameraCapabilities {
        return try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.get()
            
            CameraCapabilities(
                hasBackCamera = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA),
                hasFrontCamera = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA),
                supportsFlash = true // Most devices support flash
            )
        } catch (e: Exception) {
            CameraCapabilities()
        }
    }
    
    private fun setupCameraUseCases(
        provider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        config: CameraConfiguration
    ) {
        val preview = Preview.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setFlashMode(config.flashMode)
            .setCaptureMode(config.captureMode)
            .setJpegQuality(config.jpegQuality)
            .build()
        
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(config.lensFacing)
            .build()
        
        provider.unbindAll()
        
        camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }
    
    private fun createImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        return File(context.cacheDir, "card_capture_$timestamp.jpg")
    }
}

/**
 * Camera configuration data class
 */
data class CameraConfiguration(
    val aspectRatio: Int = AspectRatio.RATIO_4_3,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
    val jpegQuality: Int = 95
)

/**
 * Camera capabilities data class
 */
data class CameraCapabilities(
    val hasBackCamera: Boolean = false,
    val hasFrontCamera: Boolean = false,
    val supportsFlash: Boolean = false
)

