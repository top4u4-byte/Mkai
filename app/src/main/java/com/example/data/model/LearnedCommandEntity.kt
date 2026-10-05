package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learned_commands")
data class LearnedCommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawUtterance: String,
    val normalizedUtterance: String,
    val targetActionId: String,
    val successCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
