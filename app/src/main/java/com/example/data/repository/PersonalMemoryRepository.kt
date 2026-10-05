package com.example.data.repository

import com.example.data.dao.PersonalMemoryDao
import com.example.data.model.PersonalMemoryEntity
import kotlinx.coroutines.flow.Flow

class PersonalMemoryRepository(private val dao: PersonalMemoryDao) {

    val allMemories: Flow<List<PersonalMemoryEntity>> = dao.getAllMemories()
    val memoryCount: Flow<Int> = dao.getMemoryCount()

    fun searchMemories(query: String): Flow<List<PersonalMemoryEntity>> = dao.searchMemories(query)

    fun getMemoriesByCategory(category: String): Flow<List<PersonalMemoryEntity>> = dao.getMemoriesByCategory(category)

    suspend fun findMemory(key: String): PersonalMemoryEntity? = dao.findMemoryByKey(key)

    suspend fun saveMemory(
        key: String,
        value: String,
        category: String = "USER_PREFERENCE",
        notes: String = ""
    ): Long {
        val existing = dao.findMemoryByKey(key)
        return if (existing != null) {
            val updated = existing.copy(
                memoryValue = value,
                category = category,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            dao.updateMemory(updated)
            existing.id
        } else {
            val newMemory = PersonalMemoryEntity(
                category = category,
                memoryKey = key,
                memoryValue = value,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            dao.insertMemory(newMemory)
        }
    }

    suspend fun updateMemory(memory: PersonalMemoryEntity) = dao.updateMemory(memory)

    suspend fun deleteMemory(id: Long) = dao.deleteById(id)

    suspend fun deleteByKey(key: String): Boolean = dao.deleteByKey(key) > 0

    suspend fun clearAll() = dao.clearAll()
}
