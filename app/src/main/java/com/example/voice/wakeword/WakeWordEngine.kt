package com.example.voice.wakeword

import android.content.Context

enum class WakeWordEngineStatus(val label: String, val description: String) {
    DISABLED(
        "Disabled",
        "Wake word activation is turned off. Use the microphone button to activate JARVIS."
    ),
    AVAILABLE_IN_APP(
        "Active (In-App Detection)",
        "In-App Voice Match is enabled. JARVIS detects your selected wake phrase while the app is active."
    ),
    BACKGROUND_PENDING_DSP(
        "Background Standby (Hardware DSP Pending)",
        "Always-on background hotword detection requires vendor DSP hardware models and foreground service restrictions (Phase 3). In-app detection is currently operational."
    )
}

/**
 * Wake Word Engine for JARVIS Phase 2.
 * Handles phrase matching, profile selection, and reports current engine availability.
 */
class WakeWordEngine(
    context: Context,
    val preferences: WakeWordPreferences = WakeWordPreferences(context)
) : VoiceActivationEngine {

    override val activationType: ActivationType = ActivationType.HOTWORD_WAKE_WORD

    fun getStatus(): WakeWordEngineStatus {
        return if (!preferences.isEnabled) {
            WakeWordEngineStatus.DISABLED
        } else {
            WakeWordEngineStatus.AVAILABLE_IN_APP
        }
    }

    override fun startListeningForActivation(onActivated: () -> Unit) {
        // In-App listening hook
    }

    override fun stopListeningForActivation() {
        // Stop in-app listening hook
    }

    /**
     * Checks if recognized speech begins with or contains the user's selected wake phrase.
     */
    fun matchesWakePhrase(rawText: String): Boolean {
        if (!preferences.isEnabled) return false
        val phrase = preferences.selectedPhrase.trim().lowercase()
        val text = rawText.trim().lowercase()
        return text.startsWith(phrase) || text.contains(phrase)
    }

    /**
     * Strips the wake phrase from the command so the underlying instruction can be executed.
     */
    fun extractCommandAfterWakePhrase(rawText: String): String {
        val phrase = preferences.selectedPhrase.trim().lowercase()
        val text = rawText.trim()
        val lower = text.lowercase()
        return if (lower.startsWith(phrase)) {
            text.substring(phrase.length).trim().removePrefix(",").trim()
        } else {
            text
        }
    }
}
