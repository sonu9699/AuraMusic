/*
 * AuraMusic - by Nikhil
 * Licensed Under GPL-3.0
 */

package com.aura.music.ui.component

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AuraMusic premium liquid gooey metaball loading animation.
 * Uses hardware-accelerated RenderEffect on Android 12 (API 31)+ to merge overlapping circles,
 * creating a organic fluid lava-lamp effect. Falls back to beautiful pulsing orbital spheres on older APIs.
 */
@Composable
fun AuraMusicLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color? = null,
) {
    val primaryColor = color ?: MaterialTheme.colorScheme.primary
    val secondaryColor = color ?: MaterialTheme.colorScheme.secondary
    val tertiaryColor = color ?: MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "auramusic_loader_transition")

    // Continuous orbital rotation angle
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_angle"
    )

    // Breathing pulse scale for the core ball
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Instantiate RenderEffect for liquid gooey connection
    val composeRenderEffect = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(26f, 26f, Shader.TileMode.DECAL)
            val alphaContrastMatrix = android.graphics.ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 85f, -4250f // High alpha scale & offset to clamp blur edges
            ))
            val colorFilterEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(alphaContrastMatrix))
            RenderEffect.createChainEffect(colorFilterEffect, blurEffect).asComposeRenderEffect()
        } else {
            null
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (composeRenderEffect != null) {
                        this.renderEffect = composeRenderEffect
                    }
                }
        ) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)

            // Center / Core Circle (Pulsing size and color variation)
            val coreRadius = (w * 0.22f) * corePulse
            drawCircle(
                color = primaryColor,
                radius = coreRadius,
                center = center
            )

            // Orbit radius oscillates to bring satellite balls in and out, creating organic stretch
            val orbitRadius = (w * 0.28f) * (1f + 0.12f * kotlin.math.sin(angle * 2f))

            // Satellite Ball 1 (Moving in primary orbit)
            val s1Offset = Offset(
                x = center.x + orbitRadius * kotlin.math.cos(angle),
                y = center.y + orbitRadius * kotlin.math.sin(angle)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = if (composeRenderEffect == null) 0.8f else 1.0f),
                radius = w * 0.15f,
                center = s1Offset
            )

            // Satellite Ball 2 (Moving opposite in phase)
            val s2Angle = angle + kotlin.math.PI.toFloat()
            val s2Offset = Offset(
                x = center.x + orbitRadius * kotlin.math.cos(s2Angle),
                y = center.y + orbitRadius * kotlin.math.sin(s2Angle)
            )
            drawCircle(
                color = tertiaryColor.copy(alpha = if (composeRenderEffect == null) 0.8f else 1.0f),
                radius = w * 0.15f,
                center = s2Offset
            )
        }
    }
}
