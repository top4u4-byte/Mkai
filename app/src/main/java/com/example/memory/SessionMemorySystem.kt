package com.example.memory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session-based in-memory implementation of [MemorySystem].
 * Preserves the active conversation context during the app lifecycle.
 */
class SessionMemorySystem : MemorySystem {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.JARVIS,
                text = "Online and at your service, sir. How may I assist you today?"
            )
        )
    )
    override val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    override fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    override fun getRecentHistory(limit: Int): List<ChatMessage> {
        return _messages.value.takeLast(limit)
    }

    override fun clearSession() {
        _messages.value = listOf(
            ChatMessage(
                role = MessageRole.JARVIS,
                text = "Session memory purged. Ready for instructions."
            )
        )
    }
}
