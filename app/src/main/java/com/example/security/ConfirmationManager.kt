package com.example.security

/**
 * Manages user confirmations for sensitive actions (e.g., initiating phone calls, sending messages, deleting data).
 * Prepared for Phase 2 tool execution.
 */
class ConfirmationManager {

    enum class SensitivityLevel {
        LOW,       // Harmless queries, time checks, volume adjust
        MEDIUM,    // Opening standard apps, setting timers
        HIGH       // Deleting data, sending communication, toggling critical settings
    }

    data class ActionRequest(
        val actionId: String,
        val description: String,
        val sensitivity: SensitivityLevel,
        val onConfirmed: () -> Unit,
        val onDeclined: () -> Unit = {}
    )

    fun requiresConfirmation(sensitivity: SensitivityLevel): Boolean {
        return sensitivity == SensitivityLevel.HIGH
    }
}
