package com.example.voice

import android.content.Context
import android.content.SharedPreferences

class VoicePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "jarvis_voice_prefs"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_VOICE_NAME = "selected_voice_name"
        private const val KEY_GENDER_FILTER = "gender_filter"
    }

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.02f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_SPEECH_PITCH, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_PITCH, value).apply()

    var selectedVoiceName: String?
        get() = prefs.getString(KEY_VOICE_NAME, null)
        set(value) = prefs.edit().putString(KEY_VOICE_NAME, value).apply()

    var genderFilter: String
        get() = prefs.getString(KEY_GENDER_FILTER, "DEFAULT") ?: "DEFAULT"
        set(value) = prefs.edit().putString(KEY_GENDER_FILTER, value).apply()
}
