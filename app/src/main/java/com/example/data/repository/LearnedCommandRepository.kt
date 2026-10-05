package com.example.data.repository

import com.example.data.dao.LearnedCommandDao
import com.example.data.model.LearnedCommandEntity
import kotlinx.coroutines.flow.Flow

class LearnedCommandRepository(private val dao: LearnedCommandDao) {

    val allLearnedCommands: Flow<List<LearnedCommandEntity>> = dao.getAllLearnedCommands()

    suspend fun findMatch(normalizedUtterance: String): LearnedCommandEntity? {
        // Try exact match first, then fuzzy substring
        return dao.findExactMatch(normalizedUtterance) ?: dao.findFuzzyMatch(normalizedUtterance)
    }

    suspend fun recordLearnedPattern(rawUtterance: String, normalizedUtterance: String, targetActionId: String) {
        val existing = dao.findExactMatch(normalizedUtterance)
        if (existing != null) {
            val updated = existing.copy(
                successCount = existing.successCount + 1,
                timestamp = System.currentTimeMillis()
            )
            dao.updateLearnedCommand(updated)
        } else {
            val newEntry = LearnedCommandEntity(
                rawUtterance = rawUtterance,
                normalizedUtterance = normalizedUtterance,
                targetActionId = targetActionId,
                successCount = 1,
                timestamp = System.currentTimeMillis()
            )
            dao.insertLearnedCommand(newEntry)
        }
    }

    suspend fun deletePattern(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
