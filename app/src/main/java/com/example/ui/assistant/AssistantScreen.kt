package com.example.ui.assistant

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CommandSuggestions
import com.example.ui.components.FuturisticTopBar
import com.example.ui.components.JarvisCoreOrb
import com.example.ui.components.MessageList
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBlue
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.assistantState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val liveTranscription by viewModel.liveTranscription.collectAsState()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("assistant_main_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FuturisticTopBar(
                state = state,
                isApiKeyConfigured = isApiKeyConfigured,
                onApiConfigClick = { viewModel.navigateTo(Screen.API_CONFIG) },
                onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
                onClearChatClick = { viewModel.clearChat() }
            )
        },
        containerColor = JarvisBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Section: Orb & Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JarvisCoreOrb(
                    state = state,
                    audioLevel = audioLevel,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // State Description & Live Transcription
                Text(
                    text = when (state) {
                        AssistantState.IDLE -> "SYSTEM READY • AWAITING COMMAND"
                        AssistantState.LISTENING -> "AUDIO INPUT ACTIVE • LISTENING"
                        AssistantState.PROCESSING -> "ANALYZING COMMAND..."
                        AssistantState.EXECUTING -> "EXECUTING ACTION..."
                        AssistantState.SPEAKING -> "TRANSMITTING VOCAL RESPONSE..."
                        AssistantState.ERROR -> "SYSTEM ERROR • DIAGNOSTICS LOGGED"
                    },
                    color = when (state) {
                        AssistantState.IDLE -> JarvisCyan.copy(alpha = 0.8f)
                        AssistantState.LISTENING -> JarvisEmerald
                        AssistantState.PROCESSING -> com.example.ui.theme.JarvisViolet
                        AssistantState.EXECUTING -> com.example.ui.theme.JarvisBlue
                        AssistantState.SPEAKING -> JarvisCyan
                        AssistantState.ERROR -> JarvisCrimson
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                AnimatedVisibility(
                    visible = !liveTranscription.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisSurfaceVariant)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "\"${liveTranscription ?: ""}\"",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Suggestions Carousel
            CommandSuggestions(
                onSuggestionClick = { command ->
                    focusManager.clearFocus()
                    viewModel.processUserInput(command)
                }
            )

            // Conversation Messages Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MessageList(
                    messages = messages,
                    onReplayAudio = { text -> viewModel.replayMessage(text) }
                )
            }

            // Bottom Input & Voice Control Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisSurface)
                    .border(
                        width = 1.dp,
                        color = JarvisCardBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop Speaking Button (when active)
                    AnimatedVisibility(visible = isSpeaking || state == AssistantState.SPEAKING) {
                        Row {
                            IconButton(
                                onClick = { viewModel.stopSpeaking() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCrimson.copy(alpha = 0.2f))
                                    .border(1.dp, JarvisCrimson, CircleShape)
                                    .testTag("stop_speaking_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Speaking",
                                    tint = JarvisCrimson
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.inputText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("command_input_field"),
                        placeholder = {
                            Text(
                                text = "Ask or command JARVIS...",
                                color = JarvisTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            focusManager.clearFocus()
                            viewModel.sendTextMessage()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = JarvisCyan
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Text Button (if input not empty)
                    AnimatedVisibility(visible = inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendTextMessage()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                                .testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Command",
                                tint = Color(0xFF00222B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Voice Activation Button (Microphone)
                    if (inputText.isBlank()) {
                        val isListening = state == AssistantState.LISTENING
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) JarvisEmerald else JarvisCyan
                                )
                                .border(
                                    width = if (isListening) 3.dp else 1.5.dp,
                                    color = if (isListening) Color.White else JarvisCyan,
                                    shape = CircleShape
                                )
                                .testTag("mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    if (viewModel.permissionManager.hasRecordAudioPermission()) {
                                        viewModel.onMicClicked()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.size(50.dp)
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                                    tint = if (isListening) Color(0xFF00291B) else Color(0xFF00222B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Permission Rationale Dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                containerColor = JarvisSurface,
                title = {
                    Text(
                        text = "Microphone Access Required",
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Text(
                        text = viewModel.permissionManager.getMicrophonePermissionRationale(),
                        color = JarvisTextPrimary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionRationale = false
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Text("Grant Permission", color = Color(0xFF00222B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = false }) {
                        Text("Cancel", color = JarvisTextSecondary)
                    }
                }
            )
        }
    }
}
