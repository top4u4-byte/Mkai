package com.example.voice.wakeword

/**
 * Interface defining assistant activation triggers (Manual touch, Hotword/Wake word, Widget, Headset button).
 */
interface VoiceActivationEngine {
    val activationType: ActivationType
    fun startListeningForActivation(onActivated: () -> Unit)
    fun stopListeningForActivation()
}

enum class ActivationType {
    MANUAL_BUTTON,
    HOTWORD_WAKE_WORD,
    HEADSET_HOOK,
    QUICK_SETTINGS_TILE
}
