package com.example.voice

import android.content.Context
import com.example.voice.wakeword.WakeWordEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dedicated coordinator for all voice activation pathways in JARVIS Phase 3.
 * Enforces strict single-command execution lock, prevents TTS from triggering SpeechRecognizer,
 * and handles wake-word prefix extraction.
 */
class VoiceActivationManager(
    context: Context,
    val wakeWordEngine: WakeWordEngine = WakeWordEngine(context),
    private val onSpeechResult: (String) -> Unit,
    private val onPartialSpeech: (String) -> Unit = {},
    private val onError: (String) -> Unit = {},
    private val onRmsChanged: (Float) -> Unit = {}
) {

    private val speechRecognitionManager = SpeechRecognitionManager(
        context = context,
        onFinalResult = { text ->
            handleFinalSpeech(text)
        },
        onPartialResult = { partial ->
            onPartialSpeech(partial)
        },
        onErrorOccurred = { error ->
            _isListening.value = false
            isExecutionLocked.set(false)
            onError(error)
        },
        onAudioLevelChanged = { level ->
            onRmsChanged(level)
        }
    )

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Single-command execution lock (Critical Requirement #2)
    val isExecutionLocked = AtomicBoolean(false)

    fun triggerManualActivation() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun startListening() {
        if (isExecutionLocked.get()) {
            return // Action currently executing, do not interrupt with simultaneous listener
        }
        _isListening.value = true
        speechRecognitionManager.startListening()
    }

    fun stopListening() {
        _isListening.value = false
        speechRecognitionManager.stopListening()
    }

    fun cancel() {
        _isListening.value = false
        isExecutionLocked.set(false)
        speechRecognitionManager.cancel()
    }

    fun destroy() {
        _isListening.value = false
        isExecutionLocked.set(false)
        speechRecognitionManager.destroy()
    }

    private fun handleFinalSpeech(text: String) {
        _isListening.value = false

        // Check if an action is already locked
        if (isExecutionLocked.getAndSet(true)) {
            return // Ignore duplicate execution callback
        }

        // If wake word is enabled, check if the speech starts with the wake phrase and strip it
        val processedCommand = if (wakeWordEngine.preferences.isEnabled && wakeWordEngine.matchesWakePhrase(text)) {
            wakeWordEngine.extractCommandAfterWakePhrase(text)
        } else {
            text
        }

        if (processedCommand.isNotBlank()) {
            onSpeechResult(processedCommand)
        } else {
            // Only the wake phrase was spoken (e.g. "Jarvis")
            onSpeechResult("who are you")
        }
    }

    fun unlockExecution() {
        isExecutionLocked.set(false)
    }
}
