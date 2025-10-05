package com.technitedminds.wallet.presentation.components.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Camera permission component for the camera screen
 */
@Composable
fun CameraPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier
) {
    CameraPermissionHandler(
        modifier = modifier,
        onPermissionGranted = { onPermissionGranted() },
        onPermissionDenied = { 
            // Show permission denied content and call the callback
            DefaultPermissionDeniedContent()
            onPermissionDenied()
        }
    )
}

@Composable
private fun DefaultPermissionDeniedContent() {
    // This will be handled by the CameraPermissionHandler
}