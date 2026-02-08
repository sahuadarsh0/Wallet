package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Enhanced step progress indicator with animations for the Add Card Flow.
 * 
 * Displays three steps: Type, Capture, Details
 * - Current step: filled primary circle
 * - Completed steps: checkmark icon in primary circle
 * - Future steps: number in muted circle
 * - Connecting lines change color based on completion
 * 
 * @param currentStep The current step index (0, 1, or 2)
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun StepProgressIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        AppConstants.UIText.STEP_TYPE,
        AppConstants.UIText.STEP_CAPTURE,
        AppConstants.UIText.STEP_DETAILS
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        // Circles row with connectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, stepLabel ->
                // Step circle
                StepCircle(
                    stepIndex = index,
                    currentStep = currentStep,
                    modifier = Modifier
                        .size(40.dp)
                        .semantics {
                            contentDescription = when {
                                index < currentStep -> "Step ${index + 1}: $stepLabel, completed"
                                index == currentStep -> "Step ${index + 1}: $stepLabel, current step"
                                else -> "Step ${index + 1}: $stepLabel, not started"
                            }
                        }
                )

                // Connecting line (after each step except the last)
                if (index < steps.size - 1) {
                    StepConnector(
                        isCompleted = index < currentStep,
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = AppConstants.Dimensions.SPACING_SMALL)
                    )
                }
            }
        }

        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppConstants.Dimensions.SPACING_SMALL),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, stepLabel ->
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        index < currentStep -> MaterialTheme.colorScheme.primary
                        index == currentStep -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppConstants.Dimensions.SPACING_EXTRA_SMALL)
                )
            }
        }
    }
}

/**
 * Step circle component that displays different states:
 * - Completed: checkmark icon in primary circle
 * - Current: filled primary circle
 * - Future: number in muted circle
 */
@Composable
private fun StepCircle(
    stepIndex: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val isCompleted = stepIndex < currentStep
    val isCurrent = stepIndex == currentStep

    // Spring scale bounce when becoming active
    val activeScale by animateFloatAsState(
        targetValue = if (isCurrent) 1f else if (isCompleted) 0.95f else 0.85f,
        animationSpec = WalletSpring.bouncy(),
        label = "step_scale",
    )

    // Animate circle background color
    val circleColor by animateColorAsState(
        targetValue = when {
            isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = WalletSpring.snappy(),
        label = "step_circle_color",
    )

    // Animate content color
    val contentColor by animateColorAsState(
        targetValue = when {
            isCompleted || isCurrent -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = WalletSpring.snappy(),
        label = "step_content_color",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = activeScale
                scaleY = activeScale
            }
            .clip(CircleShape)
            .background(circleColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            isCompleted -> {
                // Show checkmark icon for completed steps
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
                )
            }
            isCurrent -> {
                // Show filled circle for current step (no content, just background)
                // The circle itself indicates the current step
            }
            else -> {
                // Show step number for future steps
                Text(
                    text = "${stepIndex + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Horizontal connecting line between steps.
 * Changes color based on completion status.
 */
@Composable
private fun StepConnector(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = WalletSpring.gentle(),
        label = "connector_line_color",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(lineColor)
    )
}
