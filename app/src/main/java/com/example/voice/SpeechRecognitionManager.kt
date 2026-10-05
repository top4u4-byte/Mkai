package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manages native Android SpeechRecognizer lifecycle and events.
 * Provides live audio amplitude (RMS dB) for the JARVIS animated orb and real-time speech transcription.
 */
class SpeechRecognitionManager(
    private val context: Context,
    private val onFinalResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onErrorOccurred: (String) -> Unit = {},
    private val onAudioLevelChanged: (Float) -> Unit = {}
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (!isAvailable()) {
                    val msg = "Speech recognition service is not available on this device."
                    _state.value = SpeechRecognitionState.Error(msg)
                    onErrorOccurred(msg)
                    return@post
                }

                // Clean up previous instance
                stopAndDestroy()

                _state.value = SpeechRecognitionState.Preparing

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer

                val recognizerListener = object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = SpeechRecognitionState.Listening(0f)
                    }

                    override fun onBeginningOfSpeech() {
                        _state.value = SpeechRecognitionState.Listening(0f)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize dB typically between -2 to 10 into 0f..1f for orb animation
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _state.value = SpeechRecognitionState.Listening(normalized)
                        onAudioLevelChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _state.value = SpeechRecognitionState.Processing()
                    }

                    override fun onError(errorCode: Int) {
                        val errorMessage = mapErrorCodeToMessage(errorCode)
                        _state.value = SpeechRecognitionState.Error(errorMessage)
                        onErrorOccurred(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            _state.value = SpeechRecognitionState.Success(text)
                            onFinalResult(text)
                        } else {
                            val msg = "No speech detected."
                            _state.value = SpeechRecognitionState.Idle
                            onErrorOccurred(msg)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim()
                        if (!partial.isNullOrEmpty()) {
                            _state.value = SpeechRecognitionState.Processing(partial)
                            onPartialResult(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }

                recognizer.setRecognitionListener(recognizerListener)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                recognizer.startListening(intent)
            } catch (e: Exception) {
                val err = "Failed to start speech recognizer: ${e.localizedMessage ?: "Unknown error"}"
                _state.value = SpeechRecognitionState.Error(err)
                onErrorOccurred(err)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _state.value = SpeechRecognitionState.Idle
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {}
            _state.value = SpeechRecognitionState.Idle
        }
    }

    fun destroy() {
        mainHandler.post {
            stopAndDestroy()
            _state.value = SpeechRecognitionState.Idle
        }
    }

    private fun stopAndDestroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun mapErrorCodeToMessage(code: Int): String {
        return when (code) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
            SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK -> "Network connection required for speech recognition."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech could be recognized. Please try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please wait a moment."
            SpeechRecognizer.ERROR_SERVER -> "Recognition server error. Please try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
            else -> "Speech recognition error (Code $code)."
        }
    }
}
