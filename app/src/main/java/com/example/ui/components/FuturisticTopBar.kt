package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.assistant.AssistantState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisViolet

@Composable
fun FuturisticTopBar(
    state: AssistantState,
    isApiKeyConfigured: Boolean,
    onApiConfigClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> JarvisCyan
            AssistantState.LISTENING -> JarvisEmerald
            AssistantState.PROCESSING -> JarvisViolet
            AssistantState.EXECUTING -> JarvisBlue
            AssistantState.SPEAKING -> JarvisCyan
            AssistantState.ERROR -> JarvisCrimson
        },
        label = "state_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisSurface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("futuristic_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Branding & State Pill
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = JarvisCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                // Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSurfaceVariant)
                        .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(if (state == AssistantState.IDLE) 0.8f else dotAlpha)
                            .clip(CircleShape)
                            .background(stateColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.label,
                        color = stateColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Text(
                text = "System Mk II • Personal AI",
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // API Key Status Badge / Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isApiKeyConfigured) JarvisCyan.copy(alpha = 0.12f)
                        else JarvisAmber.copy(alpha = 0.18f)
                    )
                    .border(
                        1.dp,
                        if (isApiKeyConfigured) JarvisCyan.copy(alpha = 0.4f)
                        else JarvisAmber.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onApiConfigClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("api_key_status_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Gemini Key Config",
                        tint = if (isApiKeyConfigured) JarvisCyan else JarvisAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isApiKeyConfigured) "KEY SET" else "ADD KEY",
                        color = if (isApiKeyConfigured) JarvisCyan else JarvisAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onClearChatClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Session Memory",
                    tint = JarvisTextPrimary.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = JarvisTextPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}
