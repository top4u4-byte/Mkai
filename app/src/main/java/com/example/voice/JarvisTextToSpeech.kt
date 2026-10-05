package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class JarvisVoiceInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val locale: Locale,
    val genderGuess: String // "Male", "Female", "Standard"
)

/**
 * Native Android TextToSpeech engine for JARVIS Phase 3.
 * Supports query of real installed TTS voices, Male/Female voice switching,
 * speech rate/pitch calibration, utterance lifecycle tracking, and race-condition prevention.
 */
class JarvisTextToSpeech(
    context: Context,
    val preferences: VoicePreferences = VoicePreferences(context),
    private val onInitSuccess: () -> Unit = {},
    private val onInitError: (String) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private val _availableVoicesFlow = MutableStateFlow<List<JarvisVoiceInfo>>(emptyList())
    val availableVoicesFlow: StateFlow<List<JarvisVoiceInfo>> = _availableVoicesFlow.asStateFlow()

    private var activeUtteranceId: String? = null
    private var currentOnStart: (() -> Unit)? = null
    private var currentOnDone: (() -> Unit)? = null
    private var currentOnError: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }

            tts?.setPitch(preferences.speechPitch)
            tts?.setSpeechRate(preferences.speechRate)

            loadInstalledVoices()

            // Restore saved voice if configured
            preferences.selectedVoiceName?.let { savedName ->
                setVoiceByName(savedName)
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isSpeakingFlow.value = true
                        currentOnStart?.invoke()
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isSpeakingFlow.value = false
                        activeUtteranceId = null
                        currentOnDone?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isSpeakingFlow.value = false
                        activeUtteranceId = null
                        currentOnError?.invoke("TTS error during speech playback.")
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == activeUtteranceId) {
                        _isSpeakingFlow.value = false
                        activeUtteranceId = null
                        currentOnError?.invoke("TTS playback error (code $errorCode).")
                    }
                }
            })

            isInitialized = true
            onInitSuccess()
        } else {
            isInitialized = false
            onInitError("Text-To-Speech engine failed to initialize.")
        }
    }

    private fun loadInstalledVoices() {
        try {
            val voices: Set<Voice>? = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val list = mutableListOf<JarvisVoiceInfo>()
                for (voice in voices) {
                    val nameLower = voice.name.lowercase()
                    val genderGuess = when {
                        nameLower.contains("female") || nameLower.contains("-fem-") || nameLower.contains("#female") -> "Female"
                        nameLower.contains("male") || nameLower.contains("-male-") || nameLower.contains("#male") -> "Male"
                        else -> "Standard"
                    }
                    val display = "${voice.locale.displayLanguage} (${voice.locale.country}) - $genderGuess ${voice.name.takeLast(6)}"
                    list.add(
                        JarvisVoiceInfo(
                            id = voice.name,
                            name = voice.name,
                            displayName = display,
                            locale = voice.locale,
                            genderGuess = genderGuess
                        )
                    )
                }
                _availableVoicesFlow.value = list.sortedBy { it.displayName }
            }
        } catch (_: Exception) {}
    }

    fun getAvailableVoices(): List<JarvisVoiceInfo> = _availableVoicesFlow.value

    fun setVoiceByName(voiceName: String): Boolean {
        if (!isInitialized || tts == null) return false
        val voices = tts?.voices ?: return false
        val matched = voices.find { it.name.equals(voiceName, ignoreCase = true) }
        return if (matched != null) {
            tts?.voice = matched
            preferences.selectedVoiceName = matched.name
            true
        } else {
            false
        }
    }

    fun setVoiceByGender(targetGender: String): Boolean {
        val available = _availableVoicesFlow.value
        val match = available.find { it.genderGuess.equals(targetGender, ignoreCase = true) }
        return if (match != null) {
            setVoiceByName(match.name)
        } else {
            false
        }
    }

    fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!isInitialized || tts == null) {
            onError("TTS engine is not ready yet.")
            return
        }

        // Cancel pending listeners before flushing to avoid phantom completion
        currentOnStart = null
        currentOnDone = null
        currentOnError = null

        if (isInitialized) {
            try {
                tts?.stop()
            } catch (_: Exception) {}
        }
        activeUtteranceId = null
        _isSpeakingFlow.value = false

        currentOnStart = onStart
        currentOnDone = onDone
        currentOnError = onError

        val sanitizedText = sanitizeForSpeech(text)
        val utteranceId = "jarvis_tts_${UUID.randomUUID()}"
        activeUtteranceId = utteranceId

        val params = Bundle()
        tts?.speak(sanitizedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        val previousOnDone = currentOnDone
        currentOnStart = null
        currentOnDone = null
        currentOnError = null

        if (isInitialized) {
            try {
                tts?.stop()
            } catch (_: Exception) {}
        }
        activeUtteranceId = null
        _isSpeakingFlow.value = false
        previousOnDone?.invoke()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true || _isSpeakingFlow.value
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        preferences.speechRate = clamped
        tts?.setSpeechRate(clamped)
    }

    fun setPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        preferences.speechPitch = clamped
        tts?.setPitch(clamped)
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }

    private fun sanitizeForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`{1,3}(.*?)`{1,3}"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("[-*•]\\s+"), ", ")
            .trim()
    }
}
