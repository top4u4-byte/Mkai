package com.example.command.offline

import android.content.Context
import com.example.actions.ActionId
import com.example.actions.ActionResult
import com.example.actions.AndroidActionExecutor
import com.example.data.repository.LearnedCommandRepository

/**
 * Modular Offline Command Engine for JARVIS Phase 3.
 * Normalizes input, checks natural language command aliases, consults local learned patterns,
 * and delegates execution to the centralized [AndroidActionExecutor].
 */
class OfflineCommandEngine(
    context: Context,
    val actionExecutor: AndroidActionExecutor = AndroidActionExecutor(context),
    var learnedCommandRepository: LearnedCommandRepository? = null
) {

    private val commandRegistry = mutableListOf<OfflineCommand>()

    init {
        registerBuiltInCommands()
    }

    private fun registerBuiltInCommands() {
        // --- APPLICATIONS ---
        register(
            OfflineCommand(
                id = "open_camera",
                actionId = ActionId.OPEN_CAMERA,
                description = "Opens device camera",
                aliases = listOf(
                    "open camera", "launch camera", "start camera", "take a picture",
                    "take picture", "take a photo", "take photo", "capture photo",
                    "snap photo", "open the camera", "camera"
                )
            )
        )

        register(
            OfflineCommand(
                id = "open_browser",
                actionId = ActionId.OPEN_BROWSER,
                description = "Opens default internet browser",
                aliases = listOf("open browser", "launch browser", "start browser", "web browser", "browse the web", "open internet", "go to web", "browser")
            )
        )

        register(
            OfflineCommand(
                id = "open_chrome",
                actionId = ActionId.OPEN_CHROME,
                description = "Opens Google Chrome if installed",
                aliases = listOf("open chrome", "launch chrome", "start chrome", "chrome browser", "open google chrome", "chrome")
            )
        )

        register(
            OfflineCommand(
                id = "open_youtube",
                actionId = ActionId.OPEN_YOUTUBE,
                description = "Opens YouTube if installed",
                aliases = listOf("open youtube", "launch youtube", "start youtube", "go to youtube", "take me to youtube", "play youtube", "open the youtube app", "youtube")
            )
        )

        register(
            OfflineCommand(
                id = "open_maps",
                actionId = ActionId.OPEN_MAPS,
                description = "Opens Google Maps if installed",
                aliases = listOf("open maps", "launch maps", "open google maps", "start maps", "take me to maps", "open navigation", "maps", "navigation")
            )
        )

        register(
            OfflineCommand(
                id = "open_calculator",
                actionId = ActionId.OPEN_CALCULATOR,
                description = "Opens Calculator application",
                aliases = listOf("open calculator", "launch calculator", "start calculator", "open calc", "calculator", "calc")
            )
        )

        register(
            OfflineCommand(
                id = "open_phone",
                actionId = ActionId.OPEN_PHONE,
                description = "Opens Phone dialer",
                aliases = listOf("open phone", "open dialer", "launch phone", "make a call", "phone dialer", "dialer", "phone")
            )
        )

        register(
            OfflineCommand(
                id = "open_contacts",
                actionId = ActionId.OPEN_CONTACTS,
                description = "Opens Contacts list",
                aliases = listOf("open contacts", "view contacts", "launch contacts", "open address book", "contacts", "address book")
            )
        )

        register(
            OfflineCommand(
                id = "open_messages",
                actionId = ActionId.OPEN_MESSAGES,
                description = "Opens Messaging application",
                aliases = listOf("open messages", "launch messages", "open sms", "view messages", "open texting", "messages", "sms")
            )
        )

        register(
            OfflineCommand(
                id = "open_clock",
                actionId = ActionId.OPEN_CLOCK,
                description = "Opens Clock application",
                aliases = listOf("open clock", "launch clock", "show clock", "clock")
            )
        )

        register(
            OfflineCommand(
                id = "open_settings",
                actionId = ActionId.OPEN_SETTINGS,
                description = "Opens Android system settings",
                aliases = listOf("open settings", "launch settings", "device settings", "system settings", "open device settings", "settings")
            )
        )

        register(
            OfflineCommand(
                id = "open_files",
                actionId = ActionId.OPEN_FILES,
                description = "Opens Files / Storage manager",
                aliases = listOf("open files", "launch files", "file manager", "my files", "open file manager", "files")
            )
        )

        register(
            OfflineCommand(
                id = "open_gallery",
                actionId = ActionId.OPEN_GALLERY,
                description = "Opens Gallery / Photos",
                aliases = listOf("open gallery", "open photos", "launch gallery", "view photos", "view gallery", "photo gallery", "gallery", "photos")
            )
        )

        register(
            OfflineCommand(
                id = "open_calendar",
                actionId = ActionId.OPEN_CALENDAR,
                description = "Opens Calendar application",
                aliases = listOf("open calendar", "launch calendar", "view calendar", "calendar", "my schedule")
            )
        )

        register(
            OfflineCommand(
                id = "open_stopwatch",
                actionId = ActionId.OPEN_STOPWATCH,
                description = "Opens Stopwatch in clock app",
                aliases = listOf("open stopwatch", "launch stopwatch", "start stopwatch", "stopwatch")
            )
        )

        register(
            OfflineCommand(
                id = "open_whatsapp",
                actionId = ActionId.OPEN_WHATSAPP,
                description = "Opens WhatsApp if installed",
                aliases = listOf("open whatsapp", "launch whatsapp", "whatsapp")
            )
        )

        register(
            OfflineCommand(
                id = "open_email",
                actionId = ActionId.OPEN_EMAIL,
                description = "Opens Email client",
                aliases = listOf("open email", "launch email", "open mail", "check email", "email", "gmail")
            )
        )

        register(
            OfflineCommand(
                id = "open_music",
                actionId = ActionId.OPEN_MUSIC,
                description = "Opens default Music Player",
                aliases = listOf("open music", "open music app", "launch music", "open player", "music player", "music")
            )
        )

        // --- NAVIGATION ---
        register(
            OfflineCommand(
                id = "go_home",
                actionId = ActionId.GO_HOME,
                description = "Navigates to Android home screen",
                aliases = listOf("go home", "go to home", "return home", "home screen", "take me home", "go to home screen", "minimize", "home")
            )
        )

        register(
            OfflineCommand(
                id = "go_back",
                actionId = ActionId.GO_BACK,
                description = "Navigates back via Accessibility Service",
                aliases = listOf("go back", "back", "navigate back", "previous screen", "step back")
            )
        )

        register(
            OfflineCommand(
                id = "open_recents",
                actionId = ActionId.OPEN_RECENTS,
                description = "Opens recent applications overview",
                aliases = listOf("open recent apps", "recent apps", "show recents", "app switcher", "recents")
            )
        )

        // --- HARDWARE & DEVICE CONTROLS ---
        register(
            OfflineCommand(
                id = "volume_up",
                actionId = ActionId.VOLUME_UP,
                description = "Increases media volume",
                aliases = listOf("increase volume", "volume up", "turn up volume", "raise volume", "louder", "boost volume", "turn volume up")
            )
        )

        register(
            OfflineCommand(
                id = "volume_down",
                actionId = ActionId.VOLUME_DOWN,
                description = "Decreases media volume",
                aliases = listOf("decrease volume", "volume down", "turn down volume", "lower volume", "softer", "quieter", "turn volume down")
            )
        )

        register(
            OfflineCommand(
                id = "volume_mute",
                actionId = ActionId.VOLUME_MUTE,
                description = "Mutes media volume",
                aliases = listOf("mute volume", "mute", "silence media", "turn off sound", "silence volume", "mute sound")
            )
        )

        register(
            OfflineCommand(
                id = "volume_unmute",
                actionId = ActionId.VOLUME_UNMUTE,
                description = "Restores media volume",
                aliases = listOf("unmute volume", "unmute", "restore volume", "turn on sound", "unmute sound")
            )
        )

        register(
            OfflineCommand(
                id = "flashlight_on",
                actionId = ActionId.FLASHLIGHT_ON,
                description = "Turns on device torch",
                aliases = listOf("turn on flashlight", "flashlight on", "turn on torch", "torch on", "light on", "enable flashlight", "turn on the light")
            )
        )

        register(
            OfflineCommand(
                id = "flashlight_off",
                actionId = ActionId.FLASHLIGHT_OFF,
                description = "Turns off device torch",
                aliases = listOf("turn off flashlight", "flashlight off", "turn off torch", "torch off", "light off", "disable flashlight", "turn off the light")
            )
        )

        register(
            OfflineCommand(
                id = "flashlight_toggle",
                actionId = ActionId.FLASHLIGHT_TOGGLE,
                description = "Toggles device torch",
                aliases = listOf("toggle flashlight", "toggle torch", "flashlight", "torch")
            )
        )

        register(
            OfflineCommand(
                id = "media_play_pause",
                actionId = ActionId.MEDIA_PLAY_PAUSE,
                description = "Toggles music playback",
                aliases = listOf("play music", "pause music", "resume music", "play pause", "pause media", "resume media", "toggle playback")
            )
        )

        register(
            OfflineCommand(
                id = "media_next",
                actionId = ActionId.MEDIA_NEXT,
                description = "Skips to next track",
                aliases = listOf("next song", "next track", "skip song", "next music")
            )
        )

        register(
            OfflineCommand(
                id = "media_previous",
                actionId = ActionId.MEDIA_PREVIOUS,
                description = "Plays previous track",
                aliases = listOf("previous song", "previous track", "previous music")
            )
        )

        // --- DEVICE SETTINGS SHORTCUTS ---
        register(
            OfflineCommand(
                id = "open_wifi_settings",
                actionId = ActionId.OPEN_WIFI_SETTINGS,
                description = "Opens Wi-Fi settings",
                aliases = listOf("open wifi settings", "open wi-fi settings", "open wifi", "wifi settings", "turn on wifi settings")
            )
        )

        register(
            OfflineCommand(
                id = "open_bluetooth_settings",
                actionId = ActionId.OPEN_BLUETOOTH_SETTINGS,
                description = "Opens Bluetooth settings",
                aliases = listOf("open bluetooth settings", "open bluetooth", "bluetooth settings")
            )
        )

        register(
            OfflineCommand(
                id = "open_display_settings",
                actionId = ActionId.OPEN_DISPLAY_SETTINGS,
                description = "Opens Display settings",
                aliases = listOf("open display settings", "display settings", "brightness settings", "screen settings")
            )
        )

        register(
            OfflineCommand(
                id = "open_sound_settings",
                actionId = ActionId.OPEN_SOUND_SETTINGS,
                description = "Opens Sound & Vibration settings",
                aliases = listOf("open sound settings", "sound settings", "audio settings", "ringtone settings")
            )
        )

        register(
            OfflineCommand(
                id = "open_battery_settings",
                actionId = ActionId.OPEN_BATTERY_SETTINGS,
                description = "Opens Battery & Power settings",
                aliases = listOf("open battery settings", "battery settings", "power settings", "battery")
            )
        )

        register(
            OfflineCommand(
                id = "open_app_settings",
                actionId = ActionId.OPEN_APP_SETTINGS,
                description = "Opens Application settings",
                aliases = listOf("open app settings", "app settings", "applications settings", "installed apps")
            )
        )

        // --- TIME, DATE & ALARMS ---
        register(
            OfflineCommand(
                id = "time_check",
                actionId = ActionId.TIME_CHECK,
                description = "Checks current time",
                aliases = listOf("what time is it", "what time", "current time", "tell me the time", "what is the time", "time please", "the time", "time")
            )
        )

        register(
            OfflineCommand(
                id = "date_check",
                actionId = ActionId.DATE_CHECK,
                description = "Checks current date",
                aliases = listOf("what date is it", "what is today's date", "what's today's date", "what is the date", "today's date", "tell me the date", "current date", "what date", "date")
            )
        )

        register(
            OfflineCommand(
                id = "set_timer",
                actionId = ActionId.SET_TIMER,
                description = "Sets or launches timer",
                aliases = listOf("set a timer", "set timer", "start timer", "start a timer", "open timer", "timer")
            )
        )

        register(
            OfflineCommand(
                id = "set_alarm",
                actionId = ActionId.SET_ALARM,
                description = "Sets or launches alarm",
                aliases = listOf("set an alarm", "set alarm", "create alarm", "start alarm", "open alarm", "alarm")
            )
        )

        // --- DEVICE STATUS & DIAGNOSTICS ---
        register(
            OfflineCommand(
                id = "battery_check",
                actionId = ActionId.BATTERY_CHECK,
                description = "Checks battery level and charging status",
                aliases = listOf("battery level", "battery status", "how much battery", "check battery", "what is my battery", "battery percentage", "check battery level")
            )
        )

        register(
            OfflineCommand(
                id = "network_check",
                actionId = ActionId.NETWORK_CHECK,
                description = "Checks active internet connectivity",
                aliases = listOf("network status", "wifi status", "internet status", "check network", "connection status", "check internet")
            )
        )

        register(
            OfflineCommand(
                id = "bluetooth_check",
                actionId = ActionId.BLUETOOTH_CHECK,
                description = "Checks Bluetooth state",
                aliases = listOf("bluetooth status", "is bluetooth on", "check bluetooth", "bluetooth state")
            )
        )

        // --- HANDS-FREE MESSAGE & CALL CHECKING ---
        register(
            OfflineCommand(
                id = "check_messages",
                actionId = ActionId.CHECK_MESSAGES,
                description = "Checks latest incoming message notifications",
                aliases = listOf(
                    "did i receive any messages", "check my messages", "check messages",
                    "any new messages", "read messages", "did i get any messages",
                    "who sent me the latest message", "who sent the latest message", "check notifications"
                )
            )
        )

        register(
            OfflineCommand(
                id = "check_calls",
                actionId = ActionId.CHECK_CALLS,
                description = "Checks missed calls",
                aliases = listOf(
                    "did i receive a call", "who called me", "any missed calls",
                    "check missed calls", "check my calls", "missed calls", "did someone call me"
                )
            )
        )

        // --- SYSTEM STATUS ---
        register(
            OfflineCommand(
                id = "system_status",
                actionId = ActionId.SYSTEM_STATUS,
                description = "Reports JARVIS identity and status",
                aliases = listOf("who are you", "what is your status", "system status", "status report", "systems check", "status")
            )
        )
    }

    fun register(command: OfflineCommand) {
        commandRegistry.add(command)
    }

    fun getAllCommands(): List<OfflineCommand> = commandRegistry.toList()

    fun normalize(rawInput: String): String {
        var text = rawInput.trim().lowercase()

        val prefixes = listOf(
            "jarvis, please ", "jarvis please ", "jarvis, can you ", "jarvis can you ",
            "jarvis, could you ", "jarvis could you ", "hey jarvis, ", "hey jarvis ",
            "jarvis, ", "jarvis ", "please ", "can you please ", "could you please ",
            "can you ", "could you ", "would you kindly ", "would you "
        )
        for (prefix in prefixes) {
            if (text.startsWith(prefix)) {
                text = text.removePrefix(prefix).trim()
                break
            }
        }

        text = text.replace(Regex("[?.!,]+$"), "").trim()
        return text
    }

    suspend fun match(rawInput: String): OfflineCommand? {
        val normalized = normalize(rawInput)
        if (normalized.isEmpty()) return null

        // 1. Direct exact alias match from built-in catalog
        for (cmd in commandRegistry) {
            if (cmd.aliases.any { it.equals(normalized, ignoreCase = true) }) {
                return cmd
            }
        }

        // 2. Multi-word phrase & word-boundary match
        for (cmd in commandRegistry) {
            if (cmd.aliases.any { alias ->
                    if (alias.contains(" ")) {
                        normalized.contains(alias)
                    } else if (alias.length >= 4) {
                        normalized == alias || normalized.startsWith("$alias ") ||
                            normalized.endsWith(" $alias") || normalized.contains(" $alias ")
                    } else {
                        normalized == alias
                    }
                }) {
                return cmd
            }
        }

        // 3. Learned local command patterns (Requirement #9)
        learnedCommandRepository?.let { repo ->
            val learned = repo.findMatch(normalized)
            if (learned != null) {
                val matchedAction = try {
                    ActionId.valueOf(learned.targetActionId)
                } catch (_: Exception) {
                    null
                }
                if (matchedAction != null) {
                    val matchingBuiltIn = commandRegistry.find { it.actionId == matchedAction }
                    if (matchingBuiltIn != null) {
                        return matchingBuiltIn
                    }
                }
            }
        }

        return null
    }

    suspend fun execute(command: OfflineCommand, rawInput: String): ActionResult {
        val result = actionExecutor.executeAction(command.actionId)

        // Save successfully executed command pattern into learned repository
        if (result.success && learnedCommandRepository != null) {
            val normalized = normalize(rawInput)
            learnedCommandRepository?.recordLearnedPattern(
                rawUtterance = rawInput,
                normalizedUtterance = normalized,
                targetActionId = command.actionId.name
            )
        }

        return result
    }
}
