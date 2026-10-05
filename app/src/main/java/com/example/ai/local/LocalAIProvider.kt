package com.example.ai.local

import com.example.ai.AIProvider
import com.example.memory.ChatMessage

/**
 * Placeholder architecture for future local/on-device AI models (e.g., MediaPipe GenAI, llama.cpp, LiteRT).
 * Conforms to [AIProvider] so future local models can be slotted in without modifying the rest of JARVIS.
 */
class LocalAIProvider : AIProvider {

    override val providerName: String = "Local On-Device Engine (Phase 2+ Placeholder)"

    override fun isConfigured(): Boolean {
        // Reserved for Phase 2: verify local .bin / .tflite weights are present on disk
        return false
    }

    override suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>
    ): Result<String> {
        return Result.failure(
            UnsupportedOperationException(
                "Local AI inference engine is architected for Phase 2. Please configure your Gemini API key."
            )
        )
    }

    override suspend fun testConnection(): Result<Boolean> {
        return Result.failure(
            UnsupportedOperationException("Local AI engine not yet initialized.")
        )
    }
}
