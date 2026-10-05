package com.example.voice.wakeword

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists wake-word configuration and custom wake profiles using SharedPreferences.
 */
class WakeWordPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "jarvis_wake_word_prefs"
        private const val KEY_ENABLED = "wake_word_enabled"
        private const val KEY_SELECTED_PHRASE = "selected_wake_phrase"
        private const val KEY_CUSTOM_PHRASES = "custom_wake_phrases"
        private const val KEY_AUTO_LISTEN = "auto_listening_enabled"

        val BUILT_IN_PROFILES = listOf(
            "Jarvis",
            "Hey Jarvis",
            "Iron Man",
            "Hey Iron Man",
            "Tony",
            "Hey Tony",
            "Friday",
            "Hey Friday",
            "Natasha",
            "Vision",
            "Ultron",
            "Alfred",
            "Computer",
            "Assistant",
            "Hey Assistant",
            "Nova",
            "Atlas",
            "Echo",
            "Athena",
            "Oracle",
            "KITT",
            "HAL",
            "Samantha",
            "Edith",
            "Karen",
            "Robin",
            "Ghost",
            "Max",
            "AI Assistant",
            "Wake Up"
        )
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var selectedPhrase: String
        get() = prefs.getString(KEY_SELECTED_PHRASE, "Jarvis") ?: "Jarvis"
        set(value) = prefs.edit().putString(KEY_SELECTED_PHRASE, value).apply()

    var isAutoListenEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LISTEN, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LISTEN, value).apply()

    fun getCustomPhrases(): Set<String> {
        return prefs.getStringSet(KEY_CUSTOM_PHRASES, emptySet()) ?: emptySet()
    }

    fun addCustomPhrase(phrase: String): Boolean {
        val trimmed = phrase.trim()
        if (trimmed.isEmpty()) return false
        val current = getCustomPhrases().toMutableSet()
        current.add(trimmed)
        prefs.edit().putStringSet(KEY_CUSTOM_PHRASES, current).apply()
        return true
    }

    fun editCustomPhrase(oldPhrase: String, newPhrase: String): Boolean {
        val trimmed = newPhrase.trim()
        if (trimmed.isEmpty()) return false
        val current = getCustomPhrases().toMutableSet()
        current.remove(oldPhrase)
        current.add(trimmed)
        prefs.edit().putStringSet(KEY_CUSTOM_PHRASES, current).apply()
        if (selectedPhrase == oldPhrase) {
            selectedPhrase = trimmed
        }
        return true
    }

    fun deleteCustomPhrase(phrase: String) {
        val current = getCustomPhrases().toMutableSet()
        current.remove(phrase)
        prefs.edit().putStringSet(KEY_CUSTOM_PHRASES, current).apply()
        if (selectedPhrase == phrase) {
            selectedPhrase = "Jarvis"
        }
    }

    fun getAllAvailablePhrases(): List<String> {
        val custom = getCustomPhrases().toList()
        return BUILT_IN_PROFILES + custom
    }
}
