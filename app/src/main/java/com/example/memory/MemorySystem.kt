package com.example.memory

import kotlinx.coroutines.flow.StateFlow

/**
 * Core interface for the JARVIS memory layer.
 * Allows current-session context retrieval and seamlessly expands to long-term/vector memory in future phases.
 */
interface MemorySystem {
    val messagesFlow: StateFlow<List<ChatMessage>>

    fun addMessage(message: ChatMessage)
    fun getRecentHistory(limit: Int = 10): List<ChatMessage>
    fun clearSession()
}
