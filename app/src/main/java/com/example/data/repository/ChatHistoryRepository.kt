package com.example.data.repository

import com.example.data.dao.ChatHistoryDao
import com.example.data.model.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

class ChatHistoryRepository(private val dao: ChatHistoryDao) {

    val allMessages: Flow<List<ChatHistoryEntity>> = dao.getAllMessages()
    val messageCount: Flow<Int> = dao.getMessageCount()

    fun searchMessages(query: String): Flow<List<ChatHistoryEntity>> = dao.searchMessages(query)

    suspend fun getRecentMessages(limit: Int = 10): List<ChatHistoryEntity> = dao.getRecentMessages(limit)

    suspend fun insertMessage(message: ChatHistoryEntity): Long = dao.insertMessage(message)

    suspend fun deleteMessage(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearAll()
}
