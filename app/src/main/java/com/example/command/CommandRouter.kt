package com.example.command

import com.example.ai.AIProvider
import com.example.command.offline.OfflineCommandEngine
import com.example.memory.ChatMessage

sealed interface RouteResult {
    data class OfflineExecution(
        val speechResponse: String,
        val actionTitle: String,
        val success: Boolean = true,
        val requiresAccessibility: Boolean = false
    ) : RouteResult

    data class AiExecution(
        val speechResponse: String
    ) : RouteResult

    data class Error(
        val message: String
    ) : RouteResult
}

/**
 * Centralized Command Router for JARVIS Phase 2.
 * Follows the pipeline:
 * User Voice/Text -> Normalize -> Match Offline Command -> Execute Native Action -> Return Result
 * Fallback -> Gemini AI Provider -> AI Response.
 */
class CommandRouter(
    private val offlineCommandEngine: OfflineCommandEngine,
    private val aiProvider: AIProvider
) {

    suspend fun route(
        input: String,
        conversationHistory: List<ChatMessage>
    ): RouteResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return RouteResult.Error("Command cannot be empty.")
        }

        // 1. Check Offline Command Engine
        val matchedCommand = offlineCommandEngine.match(trimmed)
        if (matchedCommand != null) {
            val actionResult = offlineCommandEngine.execute(matchedCommand, trimmed)
            return RouteResult.OfflineExecution(
                speechResponse = actionResult.spokenResponse,
                actionTitle = actionResult.actionTitle,
                success = actionResult.success,
                requiresAccessibility = actionResult.requiresAccessibility
            )
        }

        // 2. Fallback to Gemini AI Provider
        if (!aiProvider.isConfigured()) {
            return RouteResult.Error(
                "Gemini API key is required for open-ended queries. Please configure your key in Settings or the API Setup screen."
            )
        }

        val aiResult = aiProvider.generateResponse(trimmed, conversationHistory)
        return if (aiResult.isSuccess) {
            RouteResult.AiExecution(aiResult.getOrThrow())
        } else {
            val exception = aiResult.exceptionOrNull()
            RouteResult.Error(exception?.localizedMessage ?: "Failed to generate AI response.")
        }
    }
}
