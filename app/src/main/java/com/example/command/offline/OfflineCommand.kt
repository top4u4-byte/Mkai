package com.example.command.offline

import com.example.actions.ActionId

/**
 * Definition of an offline command with aliases and metadata.
 * Designed so new commands can be registered without modifying core engine logic.
 */
data class OfflineCommand(
    val id: String,
    val actionId: ActionId,
    val description: String,
    val aliases: List<String>,
    val requiredPermissions: List<String> = emptyList(),
    val confirmationRequired: Boolean = false
)
