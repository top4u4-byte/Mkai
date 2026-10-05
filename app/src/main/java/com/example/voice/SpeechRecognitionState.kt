package com.example.voice

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data object Preparing : SpeechRecognitionState
    data class Listening(val rmsDb: Float = 0f) : SpeechRecognitionState
    data class Processing(val partialText: String = "") : SpeechRecognitionState
    data class Success(val recognizedText: String) : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}
