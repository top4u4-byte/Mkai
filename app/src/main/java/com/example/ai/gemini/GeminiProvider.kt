package com.example.ai.gemini

import com.example.ai.AIProvider
import com.example.memory.ChatMessage
import com.example.memory.MessageRole
import com.example.security.SecureApiKeyStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Concrete implementation of [AIProvider] using Google's Gemini 3.5 Flash REST API.
 * Uses the user's custom API key retrieved securely from [SecureApiKeyStorage].
 */
class GeminiProvider(
    private val secureApiKeyStorage: SecureApiKeyStorage
) : AIProvider {

    override val providerName: String = "Google Gemini (gemini-3.5-flash)"

    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val SYSTEM_PROMPT =
            "You are J.A.R.V.I.S., an advanced, highly intelligent, loyal, and articulate personal AI assistant. " +
            "Your persona is calm, courteous, and precise. Respond clearly and concisely so responses sound natural when spoken aloud. " +
            "Address the user politely (you may occasionally use 'sir' in a dignified British tone). Avoid excessive markdown, asterisks, or formatting that sounds awkward when read by Text-to-Speech."
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun isConfigured(): Boolean {
        return secureApiKeyStorage.hasApiKey()
    }

    override suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = secureApiKeyStorage.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in the API setup screen.")
            )
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val requestBodyJson = buildRequestBody(prompt, conversationHistory)

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(parseApiError(response.code, responseBody))
            }

            val parsedText = parseCandidateText(responseBody)
            if (parsedText.isNullOrBlank()) {
                Result.failure(IOException("No response candidates returned by Gemini."))
            } else {
                Result.success(parsedText)
            }
        } catch (e: IOException) {
            Result.failure(IOException("Network error communicating with Gemini: ${e.localizedMessage ?: "Connection failed"}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiKey = secureApiKeyStorage.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No API key entered."))
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val testPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Respond with 'Systems online'") })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(testPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(parseApiError(response.code, responseBody))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Network connection failed. Please verify your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildRequestBody(prompt: String, conversationHistory: List<ChatMessage>): JSONObject {
        val root = JSONObject()

        // System Instruction
        val systemInstruction = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", SYSTEM_PROMPT) })
            })
        }
        root.put("systemInstruction", systemInstruction)

        // Contents (Turns with strict user/model alternation)
        val contentsArray = JSONArray()
        val turns = mutableListOf<Pair<String, String>>()

        // Exclude the current prompt from context messages if it's already recorded in conversationHistory
        val historyToUse = if (conversationHistory.lastOrNull()?.let { it.role == MessageRole.USER && it.text == prompt } == true) {
            conversationHistory.dropLast(1)
        } else {
            conversationHistory
        }

        val contextMessages = historyToUse.takeLast(8)
        for (msg in contextMessages) {
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.JARVIS -> "model"
                MessageRole.SYSTEM -> continue
            }
            if (msg.text.isNotBlank()) {
                if (turns.isNotEmpty() && turns.last().first == role) {
                    val last = turns.removeAt(turns.lastIndex)
                    turns.add(role to "${last.second}\n${msg.text}")
                } else {
                    turns.add(role to msg.text)
                }
            }
        }

        // Add current prompt
        if (turns.isNotEmpty() && turns.last().first == "user") {
            val last = turns.removeAt(turns.lastIndex)
            turns.add("user" to "${last.second}\n$prompt")
        } else {
            turns.add("user" to prompt)
        }

        for ((role, text) in turns) {
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                })
            })
        }
        root.put("contents", contentsArray)

        // Generation Config
        val generationConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 1024)
        }
        root.put("generationConfig", generationConfig)

        return root
    }

    private fun parseCandidateText(responseBody: String): String? {
        return try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            parts.getJSONObject(0).optString("text")
        } catch (_: Exception) {
            null
        }
    }

    private fun parseApiError(statusCode: Int, responseBody: String): Exception {
        return try {
            val json = JSONObject(responseBody)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message") ?: "HTTP error $statusCode"
            when (statusCode) {
                400 -> Exception("Invalid API Key or Bad Request: $message")
                403 -> Exception("Access forbidden. Please ensure Gemini API is enabled for this key.")
                404 -> Exception("Model endpoint not found. Status $statusCode")
                429 -> Exception("Gemini quota or rate limit exceeded. Please try again shortly.")
                500, 503 -> Exception("Google AI servers temporarily unavailable. Status $statusCode")
                else -> Exception("Gemini API error ($statusCode): $message")
            }
        } catch (_: Exception) {
            Exception("Gemini error ($statusCode): $responseBody")
        }
    }
}
