package com.example.command.tools

/**
 * Interface for AI function / tool calling architecture.
 * In future phases, Gemini or local models will emit structured tool calls which this engine evaluates.
 */
interface ToolEngine {
    fun getRegisteredTools(): List<ToolDefinition>
    suspend fun executeTool(name: String, parameters: Map<String, Any>): ToolExecutionResult
}

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameterSchemaJson: String
)

sealed interface ToolExecutionResult {
    data class Success(val output: String) : ToolExecutionResult
    data class Failure(val error: String) : ToolExecutionResult
}
