package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Animated LazyColumn with smooth item animations and transitions. Optimized for performance with
 * large lists.
 */
@Composable
fun <T> AnimatedLazyColumn(
        items: List<T>,
        modifier: Modifier = Modifier,
        key: ((item: T) -> Any)? = null,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        enableItemAnimations: Boolean = true,
        itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
    ) {
        items(items = items, key = key) { item ->
            if (enableItemAnimations) {
                Box(
                        modifier =
                                Modifier.animateItem(
                                        fadeInSpec = tween(300),
                                        fadeOutSpec = tween(300),
                                        placementSpec =
                                                spring(
                                                        dampingRatio =
                                                                Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                )
                                )
                ) { itemContent(item) }
            } else {
                itemContent(item)
            }
        }
    }
}

/** Animated LazyRow with smooth item animations and transitions. */
@Composable
fun <T> AnimatedLazyRow(
        items: List<T>,
        modifier: Modifier = Modifier,
        key: ((item: T) -> Any)? = null,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        enableItemAnimations: Boolean = true,
        itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    LazyRow(
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement
    ) {
        items(items = items, key = key) { item ->
            if (enableItemAnimations) {
                Box(
                        modifier =
                                Modifier.animateItem(
                                        fadeInSpec = tween(300),
                                        fadeOutSpec = tween(300),
                                        placementSpec =
                                                spring(
                                                        dampingRatio =
                                                                Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                )
                                )
                ) { itemContent(item) }
            } else {
                itemContent(item)
            }
        }
    }
}

/** Animated LazyVerticalGrid with smooth item animations. */
@Composable
fun <T> AnimatedLazyVerticalGrid(
        items: List<T>,
        columns: GridCells,
        modifier: Modifier = Modifier,
        key: ((item: T) -> Any)? = null,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        enableItemAnimations: Boolean = true,
        itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement
    ) {
        items(items = items, key = key) { item ->
            if (enableItemAnimations) {
                Box(
                        modifier =
                                Modifier.animateItem(
                                        fadeInSpec = tween(300),
                                        fadeOutSpec = tween(300),
                                        placementSpec =
                                                spring(
                                                        dampingRatio =
                                                                Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                )
                                )
                ) { itemContent(item) }
            } else {
                itemContent(item)
            }
        }
    }
}

/** Staggered animation for list items appearing sequentially */
@Composable
fun <T> StaggeredAnimatedColumn(
        items: List<T>,
        modifier: Modifier = Modifier,
        staggerDelayMillis: Int = 100,
        itemContent: @Composable (index: Int, item: T) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * staggerDelayMillis.toLong())
                isVisible = true
            }

            androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter =
                            androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec =
                                            tween(durationMillis = 500, easing = EaseOutCubic)
                            ) +
                                    androidx.compose.animation.fadeIn(
                                            animationSpec =
                                                    tween(
                                                            durationMillis = 500,
                                                            easing = EaseOutCubic
                                                    )
                                    ),
                    exit =
                            androidx.compose.animation.slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec =
                                            tween(durationMillis = 300, easing = EaseInCubic)
                            ) +
                                    androidx.compose.animation.fadeOut(
                                            animationSpec =
                                                    tween(
                                                            durationMillis = 300,
                                                            easing = EaseInCubic
                                                    )
                                    )
            ) { itemContent(index, item) }
        }
    }
}

/** Performance-optimized animated list for large datasets */
@Composable
fun <T> PerformantAnimatedList(
        items: List<T>,
        modifier: Modifier = Modifier,
        key: ((item: T) -> Any)? = null,
        contentPadding: PaddingValues = PaddingValues(16.dp),
        itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = items, key = key) { item ->
            // Simplified animation for better performance with large lists
            Box(modifier = Modifier.animateItem()) { itemContent(item) }
        }
    }
}
