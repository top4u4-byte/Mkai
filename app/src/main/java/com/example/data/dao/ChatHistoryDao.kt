package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatHistoryEntity>>

    @Query("SELECT * FROM chat_history WHERE messageText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatHistoryEntity>>

    @Query("SELECT * FROM chat_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatHistoryEntity): Long

    @Query("DELETE FROM chat_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM chat_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM chat_history")
    fun getMessageCount(): Flow<Int>
}
