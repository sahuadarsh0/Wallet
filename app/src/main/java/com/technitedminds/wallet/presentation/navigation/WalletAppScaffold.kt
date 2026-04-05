package com.technitedminds.wallet.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.technitedminds.wallet.presentation.screens.addcard.NfcCardReaderManager
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Main app scaffold with floating pill bottom navigation overlay.
 *
 * Uses a Box layout so the pill floats above content.
 * A [NestedScrollConnection] intercepts all child scroll events to
 * smoothly hide the bar on scroll-down and reveal on scroll-up,
 * matching the behaviour of modern apps.
 */
@Composable
fun WalletAppScaffold(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    nfcCardReaderManager: NfcCardReaderManager? = null,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = shouldShowBottomNavigation(currentRoute)

    val density = LocalDensity.current
    val navBarHeightPx = with(density) { 100.dp.toPx() }

    var bottomBarOffsetPx by remember { mutableFloatStateOf(0f) }

    // Reset to default visible position whenever we land on a bottom-nav destination.
    LaunchedEffect(currentRoute) {
        if (showBottomNav) {
            bottomBarOffsetPx = 0f
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                bottomBarOffsetPx = (bottomBarOffsetPx + delta).coerceIn(-navBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (!showBottomNav) navBarHeightPx else -bottomBarOffsetPx,
        animationSpec = WalletSpring.snappy(),
        label = "bottom_bar_offset",
    )

    Box(modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        WalletNavigation(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            nfcCardReaderManager = nfcCardReaderManager,
        )

        if (showBottomNav) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer { translationY = animatedOffset },
            ) {
                WalletBottomNavigation(
                    navController = navController,
                    onAddClick = {
                        navController.navigateToDetail(NavigationDestinations.AddCard.route)
                    },
                )
            }
        }
    }
}
