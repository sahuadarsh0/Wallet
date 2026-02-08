package com.technitedminds.wallet.presentation.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════
// OnboardingPinScreen — first-install welcome with optional PIN setup
// ═══════════════════════════════════════════════════════════════════════

@Composable
fun OnboardingPinScreen(
    onSetupPin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    // ── Background shimmer ─────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "onboard_bg")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )

    // ── Stagger entrance ───────────────────────────────────────────────
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val shieldScale by animateFloatAsState(
        if (appeared) 1f else 0f, WalletSpring.bouncy(), label = "shield",
    )
    val contentAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(600, delayMillis = 300), label = "content_a",
    )
    val cardsAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(500, delayMillis = 500), label = "cards_a",
    )
    val buttonsAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(500, delayMillis = 700), label = "btns_a",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(SpaceStart, SpaceEnd, SpaceStart.copy(alpha = 0.9f)),
                    start = Offset(shimmerPhase * 200f, 0f),
                    end = Offset(1000f + shimmerPhase * 200f, 2000f),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
        ) {
            Spacer(Modifier.weight(0.12f))

            // ── Animated shield icon ───────────────────────────────────
            ShieldIcon(scale = shieldScale)

            Spacer(Modifier.height(24.dp))

            // ── Welcome text ───────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            ) {
                Text(
                    "Welcome to CardVault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your digital wallet, secured entirely on your device.\nNo internet. No tracking. Just your cards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Feature highlights ─────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.graphicsLayer { alpha = cardsAlpha },
            ) {
                FeatureRow(Icons.Default.WifiOff, "100% Offline", "All data stays on your device")
                FeatureRow(Icons.Default.Shield, "Secure Storage", "PIN-protected with encrypted backups")
                FeatureRow(Icons.Default.CreditCard, "All Card Types", "Credit, debit, gift cards, memberships & more")
            }

            Spacer(Modifier.weight(0.12f))

            // ── Buttons ────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = buttonsAlpha },
            ) {
                // Primary: Set Up PIN
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetupPin()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WalletPurple,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Icon(
                        Icons.Default.OfflinePin,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Secure with PIN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Secondary: Skip
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSkip()
                    },
                ) {
                    Text(
                        "Skip for Now",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.weight(0.08f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Visual helpers
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ShieldIcon(scale: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "shield_glow_a",
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(listOf(WalletPurple.copy(alpha = glowAlpha), Color.Transparent)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(listOf(WalletPurple, WalletBlue)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = "Security",
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = WalletPurple.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}
