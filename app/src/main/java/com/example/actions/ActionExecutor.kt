package com.example.actions

enum class ActionId {
    OPEN_CAMERA,
    OPEN_BROWSER,
    OPEN_CHROME,
    OPEN_YOUTUBE,
    OPEN_MAPS,
    OPEN_CALCULATOR,
    OPEN_PHONE,
    OPEN_CONTACTS,
    OPEN_MESSAGES,
    OPEN_CLOCK,
    OPEN_SETTINGS,
    OPEN_FILES,
    OPEN_GALLERY,
    OPEN_CALENDAR,
    OPEN_STOPWATCH,
    OPEN_WHATSAPP,
    OPEN_EMAIL,
    OPEN_MUSIC,
    GO_HOME,
    GO_BACK,
    OPEN_RECENTS,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    VOLUME_UNMUTE,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_DISPLAY_SETTINGS,
    OPEN_SOUND_SETTINGS,
    OPEN_BATTERY_SETTINGS,
    OPEN_APP_SETTINGS,
    SET_TIMER,
    SET_ALARM,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    FLASHLIGHT_TOGGLE,
    MEDIA_PLAY_PAUSE,
    MEDIA_NEXT,
    MEDIA_PREVIOUS,
    BATTERY_CHECK,
    NETWORK_CHECK,
    BLUETOOTH_CHECK,
    CHECK_MESSAGES,
    CHECK_LATEST_MESSAGE,
    CHECK_CALLS,
    TIME_CHECK,
    DATE_CHECK,
    SYSTEM_STATUS
}

data class ActionResult(
    val success: Boolean,
    val spokenResponse: String,
    val actionTitle: String,
    val requiresAccessibility: Boolean = false,
    val requiresNotificationAccess: Boolean = false,
    val requiresCallLogPermission: Boolean = false,
    val isIntentDispatched: Boolean = false
)

interface ActionExecutor {
    val executorName: String
    suspend fun executeAction(actionId: ActionId, parameters: Map<String, Any> = emptyMap()): ActionResult
    fun isActionSupported(actionId: ActionId): Boolean
}
