package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChatHistoryDao
import com.example.data.dao.LearnedCommandDao
import com.example.data.dao.PersonalMemoryDao
import com.example.data.model.ChatHistoryEntity
import com.example.data.model.LearnedCommandEntity
import com.example.data.model.PersonalMemoryEntity

@Database(
    entities = [
        ChatHistoryEntity::class,
        PersonalMemoryEntity::class,
        LearnedCommandEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun personalMemoryDao(): PersonalMemoryDao
    abstract fun learnedCommandDao(): LearnedCommandDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis_core_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
