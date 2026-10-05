package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.actions.ActionId
import com.example.command.offline.OfflineCommandEngine
import com.example.voice.wakeword.WakeWordEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineCommandEngineTest {

    private lateinit var engine: OfflineCommandEngine
    private lateinit var wakeWordEngine: WakeWordEngine

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        engine = OfflineCommandEngine(context)
        wakeWordEngine = WakeWordEngine(context)
    }

    @Test
    fun testYouTubeAliasesMatchSameAction() = runBlocking {
        val aliases = listOf(
            "Open YouTube",
            "launch youtube",
            "start youtube",
            "take me to youtube",
            "jarvis, please open youtube"
        )
        for (query in aliases) {
            val matched = engine.match(query)
            assertNotNull("Failed to match for query: $query", matched)
            assertEquals("Expected OPEN_YOUTUBE action", ActionId.OPEN_YOUTUBE, matched?.actionId)
        }
    }

    @Test
    fun testCameraAliasesMatchSameAction() = runBlocking {
        val aliases = listOf(
            "Open camera",
            "launch camera",
            "start camera",
            "take a picture",
            "hey jarvis open the camera"
        )
        for (query in aliases) {
            val matched = engine.match(query)
            assertNotNull("Failed to match for query: $query", matched)
            assertEquals("Expected OPEN_CAMERA action", ActionId.OPEN_CAMERA, matched?.actionId)
        }
    }

    @Test
    fun testVolumeCommandsMatchProperActions() = runBlocking {
        assertEquals(ActionId.VOLUME_UP, engine.match("increase volume")?.actionId)
        assertEquals(ActionId.VOLUME_DOWN, engine.match("decrease volume")?.actionId)
        assertEquals(ActionId.VOLUME_MUTE, engine.match("mute volume")?.actionId)
        assertEquals(ActionId.VOLUME_UNMUTE, engine.match("unmute volume")?.actionId)
    }

    @Test
    fun testSettingsShortcutsMatchProperActions() = runBlocking {
        assertEquals(ActionId.OPEN_WIFI_SETTINGS, engine.match("open wifi settings")?.actionId)
        assertEquals(ActionId.OPEN_BLUETOOTH_SETTINGS, engine.match("open bluetooth settings")?.actionId)
        assertEquals(ActionId.OPEN_DISPLAY_SETTINGS, engine.match("open display settings")?.actionId)
        assertEquals(ActionId.OPEN_BATTERY_SETTINGS, engine.match("open battery settings")?.actionId)
    }

    @Test
    fun testFlashlightCommandsMatchProperActions() = runBlocking {
        assertEquals(ActionId.FLASHLIGHT_ON, engine.match("turn on flashlight")?.actionId)
        assertEquals(ActionId.FLASHLIGHT_OFF, engine.match("turn off torch")?.actionId)
        assertEquals(ActionId.FLASHLIGHT_TOGGLE, engine.match("toggle flashlight")?.actionId)
    }

    @Test
    fun testCommunicationAndMediaCommandsMatchProperActions() = runBlocking {
        assertEquals(ActionId.OPEN_WHATSAPP, engine.match("open whatsapp")?.actionId)
        assertEquals(ActionId.OPEN_EMAIL, engine.match("open email")?.actionId)
        assertEquals(ActionId.OPEN_MUSIC, engine.match("open music")?.actionId)
        assertEquals(ActionId.MEDIA_PLAY_PAUSE, engine.match("play music")?.actionId)
        assertEquals(ActionId.MEDIA_NEXT, engine.match("next song")?.actionId)
        assertEquals(ActionId.OPEN_CALENDAR, engine.match("open calendar")?.actionId)
        assertEquals(ActionId.OPEN_STOPWATCH, engine.match("open stopwatch")?.actionId)
        assertEquals(ActionId.BATTERY_CHECK, engine.match("battery status")?.actionId)
        assertEquals(ActionId.NETWORK_CHECK, engine.match("check network")?.actionId)
        assertEquals(ActionId.CHECK_MESSAGES, engine.match("did i receive any messages")?.actionId)
        assertEquals(ActionId.CHECK_CALLS, engine.match("did i receive a call")?.actionId)
    }

    @Test
    fun testTimeAndDateExecution() = runBlocking {
        val timeCmd = engine.match("what time is it")
        assertNotNull(timeCmd)
        val timeResult = engine.execute(timeCmd!!, "what time is it")
        assertTrue("Expected time result", timeResult.spokenResponse.contains("current time is"))

        val dateCmd = engine.match("what is today's date")
        assertNotNull(dateCmd)
        val dateResult = engine.execute(dateCmd!!, "what is today's date")
        assertTrue("Expected date result", dateResult.spokenResponse.contains("Today is"))
    }

    @Test
    fun testUnrecognizedQueryReturnsNullForGeminiFallback() = runBlocking {
        val matched = engine.match("Explain quantum computing in detail")
        assertNull("Open-ended AI prompt should not match offline command", matched)
    }

    @Test
    fun testWakeWordProfileManagement() {
        wakeWordEngine.preferences.isEnabled = true
        wakeWordEngine.preferences.selectedPhrase = "Friday"
        assertTrue(wakeWordEngine.matchesWakePhrase("Friday, open camera"))
        assertEquals("open camera", wakeWordEngine.extractCommandAfterWakePhrase("Friday, open camera"))
    }
}
