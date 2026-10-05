package com.example.command

sealed interface CommandResult {
    data class Handled(
        val responseSpeech: String,
        val actionTitle: String,
        val isIntentDispatched: Boolean = false
    ) : CommandResult

    data object NotHandled : CommandResult
}
