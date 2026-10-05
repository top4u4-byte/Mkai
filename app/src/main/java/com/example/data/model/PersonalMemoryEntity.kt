package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_memory")
data class PersonalMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "USER_PREFERENCE", "PERSONAL_PREFERENCE", "COMMAND_PATTERN", "CORRECTION", "IMPORTANT_NOTE", "SUCCESSFUL_ACTION"
    val memoryKey: String, // e.g. "music_app", "nickname", "camera_preference"
    val memoryValue: String, // e.g. "YouTube Music", "Sir", "default camera"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float = 1.0f
)
