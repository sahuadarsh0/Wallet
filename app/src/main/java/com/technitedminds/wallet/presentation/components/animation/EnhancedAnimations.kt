package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Enhanced slide-in animation for list items
 */
@Composable
fun EnhancedSlideInItem(
    visible: Boolean,
    index: Int = 0,
    baseDelay: Int = AppConstants.Animation.SLIDE_IN_DURATION_INCREMENT,
    content: @Composable () -> Unit
) {
    val delay = index * baseDelay
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = AppConstants.Animation.SLIDE_IN_DURATION_BASE + delay,
                delayMillis = delay,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AppConstants.Animation.SLIDE_IN_DURATION_BASE + delay,
                delayMillis = delay,
                easing = EaseOutCubic
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(AppConstants.Animation.EXIT_ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(AppConstants.Animation.EXIT_ANIMATION_DURATION))
    ) {
        content()
    }
}

/**
 * Enhanced shimmer loading effect
 */
@Composable
fun EnhancedShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = AppConstants.AnimationValues.LABEL_SHIMMER)
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(AppConstants.Animation.SHIMMER_ANIMATION_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = AppConstants.AnimationValues.LABEL_SHIMMER_TRANSLATE
    )

    if (isLoading) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY)
                        ),
                        start = Offset(translateAnim - 200f, 0f),
                        end = Offset(translateAnim, 0f)
                    )
                )
        )
    }
}
