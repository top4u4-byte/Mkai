package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonalMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalMemoryDao {
    @Query("SELECT * FROM personal_memory ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memory WHERE memoryKey LIKE '%' || :query || '%' OR memoryValue LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memory WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memory WHERE LOWER(memoryKey) = LOWER(:key) LIMIT 1")
    suspend fun findMemoryByKey(key: String): PersonalMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: PersonalMemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: PersonalMemoryEntity)

    @Query("DELETE FROM personal_memory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM personal_memory WHERE LOWER(memoryKey) = LOWER(:key)")
    suspend fun deleteByKey(key: String): Int

    @Query("DELETE FROM personal_memory")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM personal_memory")
    fun getMemoryCount(): Flow<Int>
}
