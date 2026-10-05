package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.ChatHistoryEntity
import com.example.data.model.PersonalMemoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadChatHistory() = runBlocking {
        val entry = ChatHistoryEntity(
            role = "USER",
            messageText = "Jarvis, what time is it?",
            isOffline = true
        )
        db.chatHistoryDao().insertMessage(entry)

        val all = db.chatHistoryDao().getAllMessages().first()
        assertEquals(1, all.size)
        assertEquals("Jarvis, what time is it?", all[0].messageText)
    }

    @Test
    fun insertAndReadPersonalMemory() = runBlocking {
        val memory = PersonalMemoryEntity(
            category = "USER_PREFERENCE",
            memoryKey = "music_app",
            memoryValue = "YouTube Music"
        )
        db.personalMemoryDao().insertMemory(memory)

        val retrieved = db.personalMemoryDao().findMemoryByKey("music_app")
        assertEquals("YouTube Music", retrieved?.memoryValue)
    }
}
