package com.example.ui.memory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalMemoryEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.allMemoriesFlow.collectAsState()
    val memoryCount by viewModel.memoryCountFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var memoryToEdit by remember { mutableStateOf<PersonalMemoryEntity?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("USER_PREFERENCE") }

    val filteredMemories = memories.filter {
        searchQuery.isBlank() ||
                it.memoryKey.contains(searchQuery, ignoreCase = true) ||
                it.memoryValue.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp)
            .testTag("memory_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                modifier = Modifier.testTag("memory_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = JarvisCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PERSONAL MEMORY",
                    color = JarvisTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$memoryCount memories stored locally in Room DB",
                    color = JarvisTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = {
                    inputKey = ""
                    inputValue = ""
                    inputCategory = "USER_PREFERENCE"
                    showAddDialog = true
                },
                modifier = Modifier.testTag("add_memory_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Memory",
                    tint = JarvisCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("memory_search_input"),
            placeholder = { Text("Search memories by key, value or category...", color = JarvisTextMuted, fontSize = 12.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisCardBorder,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Memory List
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = JarvisCyan.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No personal memories stored yet." else "No matching memories found.",
                        color = JarvisTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Say \"Jarvis, remember that...\" to create memories automatically.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("memory_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredMemories, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onEdit = {
                            memoryToEdit = memory
                            inputKey = memory.memoryKey
                            inputValue = memory.memoryValue
                            inputCategory = memory.category
                        },
                        onDelete = { viewModel.deletePersonalMemory(memory.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clear All Memories Button
        if (memories.isNotEmpty()) {
            OutlinedButton(
                onClick = { showClearConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_all_memories_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCrimson),
                border = BorderStroke(1.dp, JarvisCrimson.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Saved Memories", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = JarvisSurface,
            title = { Text("Add Personal Memory", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Key / Subject (e.g. music_app)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Value / Preference (e.g. YouTube Music)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputKey.isNotBlank() && inputValue.isNotBlank()) {
                            viewModel.savePersonalMemory(inputKey.trim(), inputValue.trim(), inputCategory)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Save", color = Color(0xFF00222B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            }
        )
    }

    // Edit Memory Dialog
    if (memoryToEdit != null) {
        AlertDialog(
            onDismissRequest = { memoryToEdit = null },
            containerColor = JarvisSurface,
            title = { Text("Edit Personal Memory", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Key / Subject") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Value / Preference") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = memoryToEdit
                        if (current != null && inputKey.isNotBlank() && inputValue.isNotBlank()) {
                            viewModel.savePersonalMemory(inputKey.trim(), inputValue.trim(), inputCategory)
                            memoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Update", color = Color(0xFF00222B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryToEdit = null }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = JarvisSurface,
            title = { Text("Clear All Memories?", color = JarvisCrimson, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all learned preferences and stored personal notes from device storage.", color = JarvisTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllPersonalMemories()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson)
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: PersonalMemoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(memory.timestamp))
    val categoryColor = when (memory.category) {
        "USER_PREFERENCE" -> JarvisCyan
        "CORRECTION" -> JarvisAmber
        "COMMAND_PATTERN" -> JarvisEmerald
        else -> JarvisViolet
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(0.5.dp, categoryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = memory.category.replace("_", " "),
                        color = categoryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = JarvisTextSecondary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = JarvisCrimson, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.memoryKey,
                color = JarvisTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = memory.memoryValue,
                color = JarvisCyan,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dateStr,
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
