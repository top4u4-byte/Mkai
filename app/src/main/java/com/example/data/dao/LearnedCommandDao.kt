package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LearnedCommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnedCommandDao {
    @Query("SELECT * FROM learned_commands ORDER BY successCount DESC, timestamp DESC")
    fun getAllLearnedCommands(): Flow<List<LearnedCommandEntity>>

    @Query("SELECT * FROM learned_commands WHERE normalizedUtterance = :normalized LIMIT 1")
    suspend fun findExactMatch(normalized: String): LearnedCommandEntity?

    @Query("SELECT * FROM learned_commands WHERE :utterance LIKE '%' || normalizedUtterance || '%' ORDER BY successCount DESC LIMIT 1")
    suspend fun findFuzzyMatch(utterance: String): LearnedCommandEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearnedCommand(command: LearnedCommandEntity): Long

    @Update
    suspend fun updateLearnedCommand(command: LearnedCommandEntity)

    @Query("DELETE FROM learned_commands WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM learned_commands")
    suspend fun clearAll()
}
