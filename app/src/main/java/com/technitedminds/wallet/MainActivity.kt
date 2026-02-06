package com.technitedminds.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.technitedminds.wallet.presentation.components.common.SplashOverlay
import com.technitedminds.wallet.presentation.navigation.WalletAppScaffold
import com.technitedminds.wallet.presentation.screens.home.EnhancedHomeScreen
import com.technitedminds.wallet.presentation.screens.settings.SettingsViewModel
import com.technitedminds.wallet.ui.theme.WalletTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the CardVault wallet app.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // System splash — dismissed immediately; Compose splash takes over
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            WalletTheme(
                themeMode = uiState.themeMode,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Main content renders behind splash so it's ready when splash exits
                    WalletAppScaffold(
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Compose splash overlay with premium animation
                    if (showSplash) {
                        SplashOverlay(
                            onSplashFinished = { showSplash = false },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    WalletTheme {
        // Preview kept simple without navigation
        EnhancedHomeScreen(onCardClick = { }, onAddCardClick = { }, modifier = Modifier.fillMaxSize())
    }
}