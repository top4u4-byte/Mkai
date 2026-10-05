package com.example.memory

import com.example.data.model.ChatHistoryEntity
import com.example.data.repository.ChatHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Persistent Room-backed implementation of [MemorySystem].
 * Ensures all user instructions and JARVIS responses survive application restarts.
 */
class PersistentMemorySystem(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val scope: CoroutineScope
) : MemorySystem {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        // Collect all messages from Room into the live UI StateFlow
        scope.launch(Dispatchers.IO) {
            chatHistoryRepository.allMessages.collect { entities ->
                val converted = if (entities.isEmpty()) {
                    listOf(
                        ChatMessage(
                            role = MessageRole.JARVIS,
                            text = "Online and at your service, sir. How may I assist you today?"
                        )
                    )
                } else {
                    entities.map { entity ->
                        ChatMessage(
                            id = entity.id.toString(),
                            role = if (entity.role == "USER") MessageRole.USER else MessageRole.JARVIS,
                            text = entity.messageText,
                            timestamp = entity.timestamp,
                            isOfflineCommand = entity.isOffline,
                            isError = entity.isError
                        )
                    }
                }
                _messages.value = converted
            }
        }
    }

    override fun addMessage(message: ChatMessage) {
        scope.launch(Dispatchers.IO) {
            val entity = ChatHistoryEntity(
                role = if (message.role == MessageRole.USER) "USER" else "JARVIS",
                messageText = message.text,
                timestamp = message.timestamp,
                isOffline = message.isOfflineCommand,
                isError = message.isError
            )
            chatHistoryRepository.insertMessage(entity)
        }
    }

    override fun getRecentHistory(limit: Int): List<ChatMessage> {
        return _messages.value.takeLast(limit)
    }

    override fun clearSession() {
        scope.launch(Dispatchers.IO) {
            chatHistoryRepository.clearHistory()
        }
    }
}
