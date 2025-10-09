package com.technitedminds.wallet

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.technitedminds.wallet.ui.theme.WalletTheme
import com.technitedminds.wallet.presentation.navigation.WalletAppScaffold
import com.technitedminds.wallet.presentation.screens.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main activity for the CardVault wallet app.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        runSplashScreenAnimation(splashScreen)

        setContent {
            WalletTheme {
                WalletAppScaffold(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun runSplashScreenAnimation(splashScreen: SplashScreen) {

        // Keep the splash screen on-screen until the content is ready.
        splashScreen.setKeepOnScreenCondition { isLoading }

        // Set up the exit animation for the splash screen.
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenViewProvider.view.height.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 300L
                doOnEnd { splashScreenViewProvider.remove() }
            }

            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.iconView,
                View.ALPHA,
                1f,
                0f
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 300L
                doOnEnd {
                    // Mark loading as complete after the exit animation finishes.
                    isLoading = false
                }
            }

            slideUp.start()
            fadeOut.start()
        }

        // Simulate a loading process for a longer animation display.
        lifecycleScope.launch {
            delay(3000) // Adjust this duration to match your animation's length
            isLoading = false // This will trigger the exit animation after the delay
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    WalletTheme {
        // Preview kept simple without navigation
        HomeScreen(onCardClick = { }, onAddCardClick = { }, modifier = Modifier.fillMaxSize())
    }
}