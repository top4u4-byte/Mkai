package com.example.ui.api

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.assistant.AssistantViewModel
import com.example.ui.assistant.Screen
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ApiKeyConfigScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val apiKeyText by viewModel.apiKeyInput.collectAsState()
    val isTesting by viewModel.isTestingApiKey.collectAsState()
    val testResult by viewModel.apiKeyTestResult.collectAsState()
    val isConfigured by viewModel.isApiKeyConfigured.collectAsState()

    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("api_key_config_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isConfigured) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.ASSISTANT) },
                    modifier = Modifier.testTag("api_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Assistant",
                        tint = JarvisCyan
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "SECURITY PROTOCOL",
                color = JarvisTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Futuristic Logo Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(JarvisSurfaceVariant)
                .border(2.dp, JarvisCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "J.A.R.V.I.S.",
            color = JarvisCyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 3.sp
        )

        Text(
            text = "Gemini API Key Configuration",
            color = JarvisTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Explanation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisSurface)
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zero-Knowledge Local Storage",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "JARVIS requires a Google Gemini API key for advanced conversational intelligence. Your key is encrypted using AES-256 GCM in your device's hardware KeyStore and is never uploaded to any intermediary servers. You can acquire a free Gemini API key from Google AI Studio.",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input Field
        OutlinedTextField(
            value = apiKeyText,
            onValueChange = { viewModel.apiKeyInput.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gemini_api_key_input"),
            label = { Text("Gemini API Key", fontFamily = FontFamily.Monospace) },
            placeholder = { Text("AIzaSy...", color = JarvisTextMuted) },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.saveApiKey()
            }),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = JarvisCyan
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible },
                    modifier = Modifier.testTag("toggle_api_visibility_button")
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide API key" else "Show API key",
                        tint = JarvisCyan
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisCardBorder,
                focusedLabelColor = JarvisCyan,
                unfocusedLabelColor = JarvisTextSecondary,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                cursorColor = JarvisCyan
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.saveApiKey()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_api_key_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = Color(0xFF00222B)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.testApiConnection()
                },
                enabled = !isTesting && apiKeyText.isNotBlank(),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("test_api_key_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisSurfaceVariant,
                    contentColor = JarvisCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = JarvisCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Testing...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Link",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clear / Delete Key Button
        OutlinedButton(
            onClick = {
                viewModel.deleteApiKey()
            },
            enabled = apiKeyText.isNotBlank() || isConfigured,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("delete_api_key_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = JarvisCrimson
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCrimson.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = JarvisCrimson,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Clear / Delete API Key",
                color = JarvisCrimson,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Test Connection Feedback Card
        AnimatedVisibility(visible = testResult != null) {
            val isSuccess = testResult?.contains("successful", ignoreCase = true) == true
            val borderColor = if (isSuccess) JarvisEmerald else JarvisCrimson
            val bgColor = if (isSuccess) JarvisEmerald.copy(alpha = 0.12f) else JarvisCrimson.copy(alpha = 0.12f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
                    .testTag("api_test_result_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = testResult ?: "",
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSuccess) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue to Assistant Button (if key is set)
        if (isConfigured) {
            Button(
                onClick = { viewModel.navigateTo(Screen.ASSISTANT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_to_assistant_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = Color(0xFF00222B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Launch J.A.R.V.I.S. Assistant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            // Can still use offline features notice
            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.ASSISTANT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("skip_to_offline_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JarvisAmber
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Proceed in Offline-Only Mode",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
