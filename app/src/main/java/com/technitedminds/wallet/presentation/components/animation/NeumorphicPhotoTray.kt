package com.technitedminds.wallet.presentation.components.animation

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * A "real card on a tray" container for image-only cards.
 *
 * Visual model:
 *   - Outer **tray**: a soft, neumorphic surface that is a bit larger than the
 *     photo. It's tinted from the card's gradient and casts a dual shadow
 *     (dark below-right, light above-left) to read as a physical pad.
 *   - Inner **card**: the captured photo, fitted (not cropped) so its real
 *     aspect ratio is preserved, lifted off the tray with its own elevation
 *     shadow. A 1px hairline rim adds the subtle "real card edge" highlight.
 *
 * The tray itself fills the parent container — the parent is responsible for
 * sizing it to the correct outer aspect ratio (e.g. via
 * `Modifier.aspectRatio(card.getDisplayAspectRatio())`). The inner photo
 * inherits the same shape and is just inset by [photoInset].
 *
 * @param imagePath  Absolute path to the captured photo (front or back).
 * @param tint       Soft tint applied to the tray surface, usually derived
 *                   from the card's gradient so the tray feels related to the
 *                   card without competing with the photo.
 * @param cornerRadius Shared rounded-corner radius for tray and photo.
 * @param photoInset  Padding between the tray edge and the photo. Larger inset
 *                    makes the photo feel more "lifted".
 */
@Composable
fun NeumorphicPhotoTray(
    imagePath: String,
    tint: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    photoInset: Dp = 12.dp,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val imageFile = remember(imagePath) {
        if (imagePath.isBlank()) null else File(imagePath).takeIf { it.exists() }
    }

    val trayShape = RoundedCornerShape(cornerRadius)
    val photoShape = RoundedCornerShape((cornerRadius.value - 4f).coerceAtLeast(4f).dp)

    // Tray gradient: slightly desaturated tint at the top, deeper towards the
    // bottom — a classic neumorphic "soft pad" look. We mix the tint with
    // white/black rather than using raw card colors so the photo stays the
    // hero element.
    val trayBrush = remember(tint) {
        Brush.linearGradient(
            colors = listOf(
                tint.lighten(0.30f),
                tint.lighten(0.10f),
                tint.darken(0.10f),
            )
        )
    }

    Box(
        modifier = modifier
            // Dual neumorphic shadow on the tray itself — dark on the
            // bottom-right, soft white highlight on the top-left.
            .neumorphicShadow(
                cornerRadius = cornerRadius,
                lightOffset = Offset(-6f, -6f),
                darkOffset = Offset(8f, 10f),
                lightColor = Color.White.copy(alpha = 0.35f),
                darkColor = Color.Black.copy(alpha = 0.35f),
                blurRadius = 18f,
            )
            .clip(trayShape)
            .background(trayBrush)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = trayShape,
            )
            .padding(photoInset),
    ) {
        // Inner photo — clipped, fit (NOT cropped) to preserve the captured
        // aspect ratio, with its own lift shadow.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .neumorphicShadow(
                    cornerRadius = (cornerRadius.value - 4f).coerceAtLeast(4f).dp,
                    lightOffset = Offset(-2f, -2f),
                    darkOffset = Offset(3f, 4f),
                    lightColor = Color.White.copy(alpha = 0.18f),
                    darkColor = Color.Black.copy(alpha = 0.45f),
                    blurRadius = 10f,
                )
                .clip(photoShape)
                .background(Color.Black.copy(alpha = 0.85f)),
        ) {
            if (imageFile != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    // CRITICAL: Fit, not Crop — preserves the captured aspect
                    // ratio. The tray + parent already match the photo shape
                    // so there's no letterboxing in practice.
                    contentScale = ContentScale.Fit,
                )
            }

            // Subtle inner rim highlight so the photo reads as a real card
            // sitting on the tray.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.18f),
                        shape = photoShape,
                    ),
            )
        }
    }
}

/**
 * Draws an outer neumorphic "soft" shadow with separate light and dark drops.
 *
 * This uses Android's [BlurMaskFilter] via [Paint] so the blur is true GPU
 * blur (cheap), instead of Compose's default elevation which is a single
 * directional shadow that doesn't read as neumorphism.
 */
private fun Modifier.neumorphicShadow(
    cornerRadius: Dp,
    lightOffset: Offset,
    darkOffset: Offset,
    lightColor: Color,
    darkColor: Color,
    blurRadius: Float,
): Modifier = this.then(
    Modifier.drawBehind {
        val cornerPx = cornerRadius.toPx()
        drawIntoCanvas { canvas ->
            // Dark drop (bottom-right)
            val darkPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = darkColor.toArgb()
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(
                darkOffset.x,
                darkOffset.y,
                size.width + darkOffset.x,
                size.height + darkOffset.y,
                cornerPx,
                cornerPx,
                darkPaint,
            )
            // Light drop (top-left)
            val lightPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = lightColor.toArgb()
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(
                lightOffset.x,
                lightOffset.y,
                size.width + lightOffset.x,
                size.height + lightOffset.y,
                cornerPx,
                cornerPx,
                lightPaint,
            )
        }
    },
)

private fun Color.lighten(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = (red + (1f - red) * f).coerceIn(0f, 1f),
        green = (green + (1f - green) * f).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * f).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

private fun Color.darken(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = (red * (1f - f)).coerceIn(0f, 1f),
        green = (green * (1f - f)).coerceIn(0f, 1f),
        blue = (blue * (1f - f)).coerceIn(0f, 1f),
        alpha = alpha,
    )
}
