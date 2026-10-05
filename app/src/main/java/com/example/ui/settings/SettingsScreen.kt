package com.example.ui.settings

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.JarvisViolet
import com.example.voice.wakeword.WakeWordPreferences

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechRate by viewModel.speechRate.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsState()

    // Wake word states
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val currentWakePhrase by viewModel.currentWakePhrase.collectAsState()
    val customWakePhrases by viewModel.customWakePhrases.collectAsState()

    // Memory & History counts
    val memoryCount by viewModel.memoryCountFlow.collectAsState()
    val historyCount by viewModel.historyCountFlow.collectAsState()

    // Permissions & Services states
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val isMicGranted by viewModel.isMicGranted.collectAsState()
    val isNotificationGranted by viewModel.isNotificationGranted.collectAsState()
    val isNotificationListenerGranted by viewModel.isNotificationListenerGranted.collectAsState()
    val isCallLogGranted by viewModel.isCallLogGranted.collectAsState()
    val isBatteryOptimized by viewModel.isBatteryOptimized.collectAsState()
    val isPocketModeActive by viewModel.isPocketModeActive.collectAsState()

    // Voice selection states
    val availableVoices by viewModel.availableVoicesFlow.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val selectedGender by viewModel.selectedVoiceGender.collectAsState()
    var isVoiceDropdownExpanded by remember { mutableStateOf(false) }

    var showAddCustomPhraseDialog by remember { mutableStateOf(false) }
    var customPhraseInput by remember { mutableStateOf("") }
    var phraseToEdit by remember { mutableStateOf<String?>(null) }
    var editPhraseInput by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissionStates() }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissionStates() }

    val callLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissionStates() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
            .testTag("settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.ASSISTANT) },
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = JarvisCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM SETTINGS",
                color = JarvisTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API Key Section Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(Screen.API_CONFIG) }
                .testTag("api_key_settings_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isApiKeyConfigured) JarvisCyan.copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (isApiKeyConfigured) JarvisCyan else JarvisAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gemini API Key",
                        color = JarvisTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isApiKeyConfigured) "Key installed (AES-256 GCM encrypted)" else "Not configured — Tap to set up",
                        color = if (isApiKeyConfigured) JarvisEmerald else JarvisAmber,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "EDIT",
                    color = JarvisCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Row: Personal Memory & Conversation History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Memory Card Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.MEMORY) }
                    .testTag("personal_memory_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Memory", color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$memoryCount items", color = JarvisTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Learned preferences", color = JarvisTextMuted, fontSize = 11.sp)
                }
            }

            // History Card Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.HISTORY) }
                    .testTag("conversation_history_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = JarvisViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", color = JarvisViolet, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$historyCount entries", color = JarvisTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Chat interaction logs", color = JarvisTextMuted, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= SCREEN-OFF / POCKET MODE =================
        Text(
            text = "HANDS-FREE POCKET MODE (SCREEN OFF)",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Foreground Audio Service", color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isPocketModeActive) "Active (Wake words & time check ready with screen off)" else "Disabled (Conserves battery)",
                            color = if (isPocketModeActive) JarvisEmerald else JarvisTextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isPocketModeActive,
                        onCheckedChange = { viewModel.togglePocketMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "When active, JARVIS runs a compliant foreground audio service with a persistent notification so you can ask \"Jarvis, what time is it?\" with the screen off.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= WAKE WORD SYSTEM =================
        Text(
            text = "WAKE WORD SYSTEM",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("wake_word_settings_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Wake Word Activation", color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isWakeWordEnabled) "Active: listening for \"$currentWakePhrase\"" else "Disabled (Manual mic active)",
                            color = if (isWakeWordEnabled) JarvisEmerald else JarvisTextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isWakeWordEnabled,
                        onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.testWakePhrase() },
                    modifier = Modifier.fillMaxWidth().testTag("test_wake_phrase_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant, contentColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f))
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Test Wake Phrase (\"$currentWakePhrase\")", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SELECT WAKE PHRASE PROFILE", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    OutlinedButton(
                        onClick = {
                            customPhraseInput = ""
                            showAddCustomPhraseDialog = true
                        },
                        border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allProfiles = WakeWordPreferences.BUILT_IN_PROFILES
                    for (phrase in allProfiles) {
                        val isSelected = currentWakePhrase.equals(phrase, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectWakePhrase(phrase) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = phrase,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Custom user profiles list
                if (customWakePhrases.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("CUSTOM PROFILES", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (custom in customWakePhrases) {
                            val isSelected = currentWakePhrase.equals(custom, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisSurfaceVariant)
                                    .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectWakePhrase(custom) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = custom,
                                        color = if (isSelected) JarvisCyan else JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            phraseToEdit = custom
                                            editPhraseInput = custom
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = JarvisTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteCustomWakePhrase(custom) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = JarvisCrimson, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= VOICE SETTINGS & MALE/FEMALE SELECTION =================
        Text(
            text = "VOICE SYNTHESIS & GENDER CALIBRATION",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("voice_settings_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Gender Filter Quick Selector
                Text("VOICE GENDER SELECTION", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val genders = listOf("DEFAULT", "MALE", "FEMALE")
                    for (g in genders) {
                        val isSelected = selectedGender.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectVoiceByGender(g) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g,
                                color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Installed TTS Voices Dropdown
                if (availableVoices.isNotEmpty()) {
                    Text("INSTALLED TTS VOICES (${availableVoices.size} FOUND)", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = isVoiceDropdownExpanded,
                        onExpandedChange = { isVoiceDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentDisplay = availableVoices.find { it.name == selectedVoiceName }?.displayName ?: selectedVoiceName ?: "Default Engine Voice"
                        OutlinedTextField(
                            value = currentDisplay,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isVoiceDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisCardBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isVoiceDropdownExpanded,
                            onDismissRequest = { isVoiceDropdownExpanded = false },
                            modifier = Modifier.background(JarvisSurface)
                        ) {
                            for (voice in availableVoices) {
                                DropdownMenuItem(
                                    text = { Text(voice.displayName, color = JarvisTextPrimary, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.selectVoiceByName(voice.name)
                                        isVoiceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Speech Rate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speech Rate", color = JarvisTextPrimary, fontSize = 14.sp)
                    }
                    Text("${String.format("%.2f", speechRate)}x", color = JarvisCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }

                Slider(
                    value = speechRate,
                    onValueChange = { viewModel.updateSpeechRate(it) },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan, inactiveTrackColor = JarvisSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Speech Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Pitch", color = JarvisTextPrimary, fontSize = 14.sp)
                    }
                    Text("${String.format("%.2f", speechPitch)}x", color = JarvisCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }

                Slider(
                    value = speechPitch,
                    onValueChange = { viewModel.updateSpeechPitch(it) },
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan, inactiveTrackColor = JarvisSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.testVoiceSpeech() },
                    modifier = Modifier.fillMaxWidth().testTag("test_voice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant, contentColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f))
                ) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Speech Output", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= PERMISSIONS & SERVICES CENTER =================
        Text(
            text = "PERMISSIONS & SERVICES CENTER",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("permissions_services_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Microphone
                ServiceStatusRow(
                    icon = Icons.Default.Mic,
                    title = "Microphone (RECORD_AUDIO)",
                    statusText = if (isMicGranted) "Granted" else "Not Granted",
                    isPositive = isMicGranted,
                    actionButtonText = if (!isMicGranted) "Grant" else null,
                    onActionClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )

                // Accessibility Service
                ServiceStatusRow(
                    icon = Icons.Default.AccessibilityNew,
                    title = "JARVIS Accessibility Service",
                    statusText = if (isAccessibilityEnabled) "Enabled" else "Disabled",
                    isPositive = isAccessibilityEnabled,
                    actionButtonText = "Open Settings",
                    onActionClick = { viewModel.permissionManager.openAccessibilitySettings() }
                )

                // Notification Listener Access (Requirement #5)
                ServiceStatusRow(
                    icon = Icons.Default.Notifications,
                    title = "Notification Listener (Read Messages)",
                    statusText = if (isNotificationListenerGranted) "Granted" else "Disabled",
                    isPositive = isNotificationListenerGranted,
                    actionButtonText = "Open Settings",
                    onActionClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                // Call Log Permission (Requirement #5)
                ServiceStatusRow(
                    icon = Icons.Default.Phone,
                    title = "Call Log Access (Missed Calls)",
                    statusText = if (isCallLogGranted) "Granted" else "Not Granted",
                    isPositive = isCallLogGranted,
                    actionButtonText = if (!isCallLogGranted) "Grant" else null,
                    onActionClick = { callLogLauncher.launch(Manifest.permission.READ_CALL_LOG) }
                )

                // Notifications
                ServiceStatusRow(
                    icon = Icons.Default.Notifications,
                    title = "Post Notifications",
                    statusText = if (isNotificationGranted) "Granted" else "Not Granted",
                    isPositive = isNotificationGranted,
                    actionButtonText = if (!isNotificationGranted && android.os.Build.VERSION.SDK_INT >= 33) "Grant" else null,
                    onActionClick = {
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )

                // Battery Optimization
                ServiceStatusRow(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Battery Optimization",
                    statusText = if (isBatteryOptimized) "Exempted" else "Optimized",
                    isPositive = isBatteryOptimized,
                    actionButtonText = "Open Settings",
                    onActionClick = { viewModel.permissionManager.openBatterySettings() }
                )

                // Overlay
                ServiceStatusRow(
                    icon = Icons.Default.Layers,
                    title = "System Overlay (Draw Over Apps)",
                    statusText = if (viewModel.permissionManager.canDrawOverlays()) "Granted" else "Not Granted",
                    isPositive = viewModel.permissionManager.canDrawOverlays(),
                    actionButtonText = "Open Settings",
                    onActionClick = { viewModel.permissionManager.openOverlaySettings() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Architecture Status
        Text(
            text = "SYSTEM ARCHITECTURE STATUS",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ArchitectureStatusItem(title = "Single Command = One Action Pipeline", status = "ENFORCED", isDone = true)
                ArchitectureStatusItem(title = "Offline Command Engine (40+ Actions)", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "Hands-free Time & Screen-Off Service", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "Incoming Calls & Message Check", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "Persistent Room Memory & Chat History", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "Local Command Learning & Retrieval", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "Audited Centralized TTS & Voice Picker", status = "ACTIVE", isDone = true)
                ArchitectureStatusItem(title = "JarvisAccessibilityService Foundation", status = "ACTIVE", isDone = true)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Add Custom Wake Phrase Dialog
    if (showAddCustomPhraseDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomPhraseDialog = false },
            containerColor = JarvisSurface,
            title = { Text("Add Custom Wake Phrase", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = {
                Column {
                    Text("Enter a unique wake phrase (e.g. \"Krypton\", \"Matrix\", \"Athena\"):", color = JarvisTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customPhraseInput,
                        onValueChange = { customPhraseInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        placeholder = { Text("Custom phrase...", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customPhraseInput.isNotBlank()) {
                            viewModel.addCustomWakePhrase(customPhraseInput.trim())
                            showAddCustomPhraseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Add", color = Color(0xFF00222B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomPhraseDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            }
        )
    }

    // Edit Custom Wake Phrase Dialog
    if (phraseToEdit != null) {
        AlertDialog(
            onDismissRequest = { phraseToEdit = null },
            containerColor = JarvisSurface,
            title = { Text("Edit Custom Wake Phrase", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = {
                Column {
                    Text("Modify wake phrase:", color = JarvisTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPhraseInput,
                        onValueChange = { editPhraseInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val old = phraseToEdit
                        if (old != null && editPhraseInput.isNotBlank()) {
                            viewModel.wakeWordEngine.preferences.editCustomPhrase(old, editPhraseInput.trim())
                            viewModel.customWakePhrases.value = viewModel.wakeWordEngine.preferences.getCustomPhrases().toList()
                            viewModel.currentWakePhrase.value = viewModel.wakeWordEngine.preferences.selectedPhrase
                            phraseToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Save", color = Color(0xFF00222B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { phraseToEdit = null }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ServiceStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    statusText: String,
    isPositive: Boolean,
    actionButtonText: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(JarvisSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPositive) JarvisEmerald else JarvisTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = statusText,
                    color = if (isPositive) JarvisEmerald else JarvisAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (actionButtonText != null) {
            OutlinedButton(
                onClick = onActionClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(actionButtonText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
fun ArchitectureStatusItem(
    title: String,
    status: String,
    isDone: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Build,
                contentDescription = null,
                tint = if (isDone) JarvisEmerald else JarvisTextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 12.sp
            )
        }
        Text(
            text = status,
            color = if (isDone) JarvisEmerald else JarvisTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
