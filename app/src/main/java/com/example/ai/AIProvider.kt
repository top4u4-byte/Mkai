package com.example.ai

import com.example.memory.ChatMessage

/**
 * Common abstraction for all AI engines in JARVIS.
 * Decouples the UI and Command router from specific cloud or local models.
 */
interface AIProvider {
    val providerName: String

    /**
     * Checks if the provider has all necessary configuration (e.g., API key, model file).
     */
    fun isConfigured(): Boolean

    /**
     * Generates a conversational response given a user prompt and recent conversation history.
     */
    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>
    ): Result<String>

    /**
     * Pings the AI engine to verify that the credentials / connectivity are functional.
     */
    suspend fun testConnection(): Result<Boolean>
}
