package com.example.security

/**
 * Validates that requested actions adhere to safety policies.
 * Prevents arbitrary shell commands, raw code execution, or unauthorized privileged operations.
 */
class ActionValidator {

    private val blockedKeywords = listOf(
        "rm -rf", "su ", "sudo", "reboot", "format", "chmod", "sh ", "bash "
    )

    fun isSafeCommand(input: String): Boolean {
        val lower = input.lowercase()
        return blockedKeywords.none { lower.contains(it) }
    }
}
