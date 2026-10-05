package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "USER" or "JARVIS"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val commandType: String? = null,
    val executionResult: String? = null,
    val isOffline: Boolean = false,
    val isError: Boolean = false,
    val sessionId: String = "default"
)
