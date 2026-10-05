package com.example.ui.assistant

enum class AssistantState(val label: String) {
    IDLE("IDLE"),
    LISTENING("LISTENING"),
    PROCESSING("PROCESSING"),
    EXECUTING("EXECUTING"),
    SPEAKING("SPEAKING"),
    ERROR("ERROR")
}

enum class Screen {
    ASSISTANT,
    API_CONFIG,
    SETTINGS,
    MEMORY,
    HISTORY
}
