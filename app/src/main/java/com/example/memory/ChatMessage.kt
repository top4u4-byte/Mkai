package com.example.memory

import java.util.UUID

enum class MessageRole {
    USER,
    JARVIS,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineCommand: Boolean = false,
    val isError: Boolean = false
)
