package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.assistant.AssistantState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated futuristic Arc-Reactor Core Indicator for JARVIS Phase 2.
 * Visually communicates states: IDLE, LISTENING, PROCESSING, EXECUTING, SPEAKING, ERROR.
 */
@Composable
fun JarvisCoreOrb(
    state: AssistantState,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Outer tech ring rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.PROCESSING -> 4000
                    AssistantState.EXECUTING -> 3000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Inner ring counter rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.PROCESSING -> 2500
                    AssistantState.EXECUTING -> 2000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // State color transition
    val coreColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> JarvisCyan
            AssistantState.LISTENING -> JarvisEmerald
            AssistantState.PROCESSING -> JarvisViolet
            AssistantState.EXECUTING -> JarvisBlue
            AssistantState.SPEAKING -> JarvisCyan
            AssistantState.ERROR -> JarvisCrimson
        },
        animationSpec = tween(350),
        label = "core_color"
    )

    val glowColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> JarvisCyan.copy(alpha = 0.35f)
            AssistantState.LISTENING -> JarvisEmerald.copy(alpha = 0.55f)
            AssistantState.PROCESSING -> JarvisViolet.copy(alpha = 0.6f)
            AssistantState.EXECUTING -> JarvisBlue.copy(alpha = 0.65f)
            AssistantState.SPEAKING -> JarvisCyan.copy(alpha = 0.5f)
            AssistantState.ERROR -> JarvisAmber.copy(alpha = 0.6f)
        },
        animationSpec = tween(350),
        label = "glow_color"
    )

    // Reactive scale
    val audioScale by animateFloatAsState(
        targetValue = when (state) {
            AssistantState.LISTENING -> 1.0f + (audioLevel * 0.45f)
            AssistantState.SPEAKING -> 1.05f + (breathingPulse - 0.88f) * 0.5f
            AssistantState.EXECUTING -> 1.1f
            else -> breathingPulse
        },
        animationSpec = tween(120),
        label = "audio_scale"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag("jarvis_core_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.85f

            // 1. Ambient outer glow gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = baseRadius * audioScale * 1.3f
                ),
                radius = baseRadius * audioScale * 1.3f,
                center = center
            )

            // 2. Outer dashed tech ring
            rotate(outerRotation, center) {
                drawCircle(
                    color = coreColor.copy(alpha = 0.35f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f, 32f, 16f), 0f)
                    )
                )

                // 4 Cardinal markers
                val markerRadius = baseRadius
                for (i in 0..3) {
                    val angle = (i * 90) * (PI / 180.0)
                    val markerPos = Offset(
                        x = (center.x + markerRadius * cos(angle)).toFloat(),
                        y = (center.y + markerRadius * sin(angle)).toFloat()
                    )
                    drawCircle(
                        color = coreColor.copy(alpha = 0.8f),
                        radius = 2.5.dp.toPx(),
                        center = markerPos
                    )
                }
            }

            // 3. Middle segmented ring (counter rotating)
            rotate(innerRotation, center) {
                drawCircle(
                    color = coreColor.copy(alpha = 0.55f),
                    radius = baseRadius * 0.76f,
                    center = center,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), 0f)
                    )
                )
            }

            // 4. Arc-reactor inner core boundary
            drawCircle(
                color = coreColor.copy(alpha = 0.7f),
                radius = baseRadius * 0.52f * audioScale,
                center = center,
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 5. Central reactor orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        coreColor.copy(alpha = 0.85f),
                        coreColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.42f * audioScale
                ),
                radius = baseRadius * 0.42f * audioScale,
                center = center
            )

            // 6. Micro Core Center
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx() * audioScale,
                center = center
            )
        }
    }
}
