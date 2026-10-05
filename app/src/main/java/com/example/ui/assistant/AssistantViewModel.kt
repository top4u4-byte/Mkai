package com.example.ui.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.gemini.GeminiProvider
import com.example.command.CommandRouter
import com.example.command.RouteResult
import com.example.command.offline.OfflineCommandEngine
import com.example.data.AppDatabase
import com.example.data.model.ChatHistoryEntity
import com.example.data.model.PersonalMemoryEntity
import com.example.data.repository.ChatHistoryRepository
import com.example.data.repository.LearnedCommandRepository
import com.example.data.repository.PersonalMemoryRepository
import com.example.memory.ChatMessage
import com.example.memory.MessageRole
import com.example.memory.PersistentMemorySystem
import com.example.notifications.JarvisNotificationListenerService
import com.example.security.PermissionManager
import com.example.security.SecureApiKeyStorage
import com.example.telephony.CallCheckManager
import com.example.voice.JarvisTextToSpeech
import com.example.voice.JarvisVoiceInfo
import com.example.voice.VoiceActivationManager
import com.example.voice.service.JarvisVoiceService
import com.example.voice.wakeword.WakeWordEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    // Database & Repositories
    private val database = AppDatabase.getInstance(application)
    val chatHistoryRepository = ChatHistoryRepository(database.chatHistoryDao())
    val personalMemoryRepository = PersonalMemoryRepository(database.personalMemoryDao())
    val learnedCommandRepository = LearnedCommandRepository(database.learnedCommandDao())

    // Memory and AI Systems
    private val memorySystem = PersistentMemorySystem(chatHistoryRepository, viewModelScope)
    val secureApiKeyStorage = SecureApiKeyStorage(application)
    val permissionManager = PermissionManager(application)
    val callCheckManager = CallCheckManager(application)
    val geminiProvider = GeminiProvider(secureApiKeyStorage)

    // Offline Command Engine wired to Learned Repository
    val offlineCommandEngine = OfflineCommandEngine(
        context = application,
        learnedCommandRepository = learnedCommandRepository
    )
    private val commandRouter = CommandRouter(offlineCommandEngine, geminiProvider)

    // Wake Word Engine & Preferences
    val wakeWordEngine = WakeWordEngine(application)

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow(
        if (secureApiKeyStorage.hasApiKey()) Screen.ASSISTANT else Screen.API_CONFIG
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Assistant State: IDLE, LISTENING, PROCESSING, EXECUTING, SPEAKING, ERROR
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    // Live Voice Level (0.0 to 1.0)
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    // Live partial recognized speech
    private val _liveTranscription = MutableStateFlow<String?>(null)
    val liveTranscription: StateFlow<String?> = _liveTranscription.asStateFlow()

    // User Text Input
    val inputText = MutableStateFlow("")

    // Status / Toast Messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // API Key Screen State
    val apiKeyInput = MutableStateFlow(secureApiKeyStorage.getApiKey())
    private val _isTestingApiKey = MutableStateFlow(false)
    val isTestingApiKey: StateFlow<Boolean> = _isTestingApiKey.asStateFlow()

    private val _apiKeyTestResult = MutableStateFlow<String?>(null)
    val apiKeyTestResult: StateFlow<String?> = _apiKeyTestResult.asStateFlow()

    private val _isApiKeyConfigured = MutableStateFlow(secureApiKeyStorage.hasApiKey())
    val isApiKeyConfigured: StateFlow<Boolean> = _isApiKeyConfigured.asStateFlow()

    // Conversation History (Persistent via Room)
    val messages: StateFlow<List<ChatMessage>> = memorySystem.messagesFlow

    val allHistoryFlow: StateFlow<List<ChatHistoryEntity>> = chatHistoryRepository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyCountFlow: StateFlow<Int> = chatHistoryRepository.messageCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allMemoriesFlow: StateFlow<List<PersonalMemoryEntity>> = personalMemoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryCountFlow: StateFlow<Int> = personalMemoryRepository.memoryCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Voice Engine & Settings
    val speechRate = MutableStateFlow(1.02f)
    val speechPitch = MutableStateFlow(0.95f)

    private val tts: JarvisTextToSpeech = JarvisTextToSpeech(
        context = application,
        onInitSuccess = {
            speechRate.value = tts.preferences.speechRate
            speechPitch.value = tts.preferences.speechPitch
            selectedVoiceName.value = tts.preferences.selectedVoiceName
        },
        onInitError = { errorMsg -> _toastMessage.value = errorMsg }
    )
    val isTtsSpeaking: StateFlow<Boolean> = tts.isSpeakingFlow
    val availableVoicesFlow: StateFlow<List<JarvisVoiceInfo>> = tts.availableVoicesFlow
    val selectedVoiceName = MutableStateFlow(tts.preferences.selectedVoiceName)
    val selectedVoiceGender = MutableStateFlow(tts.preferences.genderFilter)

    // Wake Word UI State
    val isWakeWordEnabled = MutableStateFlow(wakeWordEngine.preferences.isEnabled)
    val currentWakePhrase = MutableStateFlow(wakeWordEngine.preferences.selectedPhrase)
    val customWakePhrases = MutableStateFlow(wakeWordEngine.preferences.getCustomPhrases().toList())

    // Permissions & Services State
    val isAccessibilityEnabled = MutableStateFlow(permissionManager.isAccessibilityEnabled())
    val isMicGranted = MutableStateFlow(permissionManager.hasRecordAudioPermission())
    val isNotificationGranted = MutableStateFlow(permissionManager.hasNotificationPermission())
    val isNotificationListenerGranted = MutableStateFlow(JarvisNotificationListenerService.isAccessGranted(application))
    val isCallLogGranted = MutableStateFlow(callCheckManager.hasCallLogPermission())
    val isBatteryOptimized = MutableStateFlow(permissionManager.isBatteryOptimizationIgnored())
    val isPocketModeActive = MutableStateFlow(JarvisVoiceService.isServiceRunning)

    // Voice Activation Coordinator (with Single-Command Execution Lock)
    private val voiceActivationManager = VoiceActivationManager(
        context = application,
        wakeWordEngine = wakeWordEngine,
        onSpeechResult = { recognizedText ->
            _liveTranscription.value = null
            processUserInput(recognizedText)
        },
        onPartialSpeech = { partial ->
            _liveTranscription.value = partial
        },
        onError = { errorMsg ->
            _liveTranscription.value = null
            _assistantState.value = AssistantState.ERROR
            _toastMessage.value = errorMsg
            viewModelScope.launch {
                delay(2500)
                if (_assistantState.value == AssistantState.ERROR) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        },
        onRmsChanged = { level ->
            _audioLevel.value = level
        }
    )

    fun navigateTo(screen: Screen) {
        refreshPermissionStates()
        _currentScreen.value = screen
    }

    fun onBackNavigation() {
        if (_currentScreen.value != Screen.ASSISTANT) {
            _currentScreen.value = Screen.ASSISTANT
        }
    }

    fun refreshPermissionStates() {
        isAccessibilityEnabled.value = permissionManager.isAccessibilityEnabled()
        isMicGranted.value = permissionManager.hasRecordAudioPermission()
        isNotificationGranted.value = permissionManager.hasNotificationPermission()
        isNotificationListenerGranted.value = JarvisNotificationListenerService.isAccessGranted(getApplication())
        isCallLogGranted.value = callCheckManager.hasCallLogPermission()
        isBatteryOptimized.value = permissionManager.isBatteryOptimizationIgnored()
        isPocketModeActive.value = JarvisVoiceService.isServiceRunning
    }

    // Voice Interaction Trigger
    fun onMicClicked() {
        if (_assistantState.value == AssistantState.SPEAKING) {
            stopSpeaking()
            return
        }

        if (_assistantState.value == AssistantState.LISTENING) {
            stopListening()
            return
        }

        startListening()
    }

    fun startListening() {
        if (!permissionManager.hasRecordAudioPermission()) {
            _toastMessage.value = "Microphone permission required for voice input."
            return
        }

        stopSpeaking()
        _assistantState.value = AssistantState.LISTENING
        _liveTranscription.value = "Listening..."
        voiceActivationManager.startListening()
    }

    fun stopListening() {
        voiceActivationManager.stopListening()
        _liveTranscription.value = null
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    // Command / Text Processing (Single Command = One Action)
    fun sendTextMessage() {
        if (voiceActivationManager.isExecutionLocked.get()) return
        val text = inputText.value.trim()
        if (text.isEmpty()) return
        inputText.value = ""
        voiceActivationManager.isExecutionLocked.set(true)
        processUserInput(text)
    }

    fun processUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        stopSpeaking()

        // 1. Record user message to Room
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            text = trimmed
        )
        memorySystem.addMessage(userMessage)

        // 2. Set PROCESSING state (single-action pipeline)
        _assistantState.value = AssistantState.PROCESSING

        viewModelScope.launch {
            // Check for explicit "remember that [key] is [value]" or "remember [X]"
            if (trimmed.startsWith("remember that ", ignoreCase = true) || trimmed.startsWith("remember ", ignoreCase = true)) {
                handleExplicitMemoryCommand(trimmed)
                voiceActivationManager.unlockExecution()
                return@launch
            }

            // Check offline matching first
            val matchedOffline = offlineCommandEngine.match(trimmed)
            if (matchedOffline != null) {
                _assistantState.value = AssistantState.EXECUTING
                delay(100)
            }

            val result = commandRouter.route(trimmed, memorySystem.getRecentHistory())
            when (result) {
                is RouteResult.OfflineExecution -> {
                    val jarvisMsg = ChatMessage(
                        role = MessageRole.JARVIS,
                        text = result.speechResponse,
                        isOfflineCommand = true,
                        isError = !result.success
                    )
                    memorySystem.addMessage(jarvisMsg)
                    speakAloud(result.speechResponse)

                    if (result.requiresAccessibility) {
                        _toastMessage.value = "Please enable JARVIS Accessibility Service in Settings."
                    }
                }

                is RouteResult.AiExecution -> {
                    val jarvisMsg = ChatMessage(
                        role = MessageRole.JARVIS,
                        text = result.speechResponse
                    )
                    memorySystem.addMessage(jarvisMsg)
                    speakAloud(result.speechResponse)
                }

                is RouteResult.Error -> {
                    _assistantState.value = AssistantState.ERROR
                    val errorMsg = ChatMessage(
                        role = MessageRole.JARVIS,
                        text = result.message,
                        isError = true
                    )
                    memorySystem.addMessage(errorMsg)
                    _toastMessage.value = result.message

                    delay(2500)
                    if (_assistantState.value == AssistantState.ERROR) {
                        _assistantState.value = AssistantState.IDLE
                    }
                }
            }

            // Unlock single-action execution state once pipeline completes
            voiceActivationManager.unlockExecution()
        }
    }

    private suspend fun handleExplicitMemoryCommand(text: String) {
        val clean = text.removePrefix("remember that ").removePrefix("remember ").trim()
        val parts = clean.split(" is ", " to ", " as ", limit = 2)
        val key: String
        val value: String
        if (parts.size == 2) {
            key = parts[0].trim()
            value = parts[1].trim()
        } else {
            key = "note_${System.currentTimeMillis() % 1000}"
            value = clean
        }

        personalMemoryRepository.saveMemory(key, value, category = "USER_PREFERENCE")
        val response = "I have committed that to memory, sir: $key is $value."
        val jarvisMsg = ChatMessage(role = MessageRole.JARVIS, text = response, isOfflineCommand = true)
        memorySystem.addMessage(jarvisMsg)
        speakAloud(response)
    }

    private fun speakAloud(text: String) {
        _assistantState.value = AssistantState.SPEAKING
        tts.setSpeechRate(speechRate.value)
        tts.setPitch(speechPitch.value)
        tts.speak(
            text = text,
            onStart = {
                _assistantState.value = AssistantState.SPEAKING
                voiceActivationManager.stopListening() // Never listen to own speech
            },
            onDone = {
                _assistantState.value = AssistantState.IDLE
            },
            onError = { _ ->
                _assistantState.value = AssistantState.IDLE
            }
        )
    }

    fun stopSpeaking() {
        tts.stop()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun replayMessage(text: String) {
        stopSpeaking()
        speakAloud(text)
    }

    fun clearChat() {
        stopSpeaking()
        memorySystem.clearSession()
        _toastMessage.value = "Chat session cleared."
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    // API Key Management
    fun saveApiKey() {
        val key = apiKeyInput.value.trim()
        if (key.isEmpty()) {
            _apiKeyTestResult.value = "Please enter a valid Gemini API key."
            return
        }
        secureApiKeyStorage.saveApiKey(key)
        _isApiKeyConfigured.value = true
        _apiKeyTestResult.value = "API key saved securely."
        _toastMessage.value = "API key encrypted & saved."
    }

    fun testApiConnection() {
        val keyToTest = apiKeyInput.value.trim()
        if (keyToTest.isEmpty()) {
            _apiKeyTestResult.value = "Please enter an API key to test."
            return
        }

        secureApiKeyStorage.saveApiKey(keyToTest)
        _isApiKeyConfigured.value = true

        _isTestingApiKey.value = true
        _apiKeyTestResult.value = "Testing connection with Gemini 3.5 Flash..."

        viewModelScope.launch {
            val result = geminiProvider.testConnection()
            _isTestingApiKey.value = false
            if (result.isSuccess) {
                _apiKeyTestResult.value = "Gemini connection successful."
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Connection test failed."
                _apiKeyTestResult.value = err
            }
        }
    }

    fun deleteApiKey() {
        secureApiKeyStorage.clearApiKey()
        apiKeyInput.value = ""
        _isApiKeyConfigured.value = false
        _apiKeyTestResult.value = "API key cleared from device."
        _toastMessage.value = "API key removed."
    }

    // Wake Word Management
    fun setWakeWordEnabled(enabled: Boolean) {
        wakeWordEngine.preferences.isEnabled = enabled
        isWakeWordEnabled.value = enabled
        _toastMessage.value = if (enabled) "Wake word enabled (${currentWakePhrase.value})" else "Wake word disabled"
    }

    fun selectWakePhrase(phrase: String) {
        wakeWordEngine.preferences.selectedPhrase = phrase
        currentWakePhrase.value = phrase
        _toastMessage.value = "Wake phrase set to \"$phrase\""
    }

    fun addCustomWakePhrase(phrase: String) {
        val ok = wakeWordEngine.preferences.addCustomPhrase(phrase)
        if (ok) {
            customWakePhrases.value = wakeWordEngine.preferences.getCustomPhrases().toList()
            selectWakePhrase(phrase.trim())
            _toastMessage.value = "Custom wake phrase \"${phrase.trim()}\" added."
        }
    }

    fun deleteCustomWakePhrase(phrase: String) {
        wakeWordEngine.preferences.deleteCustomPhrase(phrase)
        customWakePhrases.value = wakeWordEngine.preferences.getCustomPhrases().toList()
        currentWakePhrase.value = wakeWordEngine.preferences.selectedPhrase
        _toastMessage.value = "Wake phrase \"$phrase\" deleted."
    }

    fun testWakePhrase() {
        speakAloud("Wake phrase test confirmed. Hello, sir, I am listening for \"${currentWakePhrase.value}\".")
    }

    // Pocket Mode / Hands-Free Service
    fun togglePocketMode(enabled: Boolean) {
        if (enabled) {
            JarvisVoiceService.startService(getApplication())
            isPocketModeActive.value = true
            _toastMessage.value = "Hands-free pocket mode service started"
        } else {
            JarvisVoiceService.stopService(getApplication())
            isPocketModeActive.value = false
            _toastMessage.value = "Hands-free pocket mode service stopped"
        }
    }

    // Voice Engine Controls
    fun updateSpeechRate(rate: Float) {
        speechRate.value = rate
        tts.setSpeechRate(rate)
    }

    fun updateSpeechPitch(pitch: Float) {
        speechPitch.value = pitch
        tts.setPitch(pitch)
    }

    fun selectVoiceByName(name: String) {
        val success = tts.setVoiceByName(name)
        if (success) {
            selectedVoiceName.value = name
            _toastMessage.value = "Voice updated."
        }
    }

    fun selectVoiceByGender(gender: String) {
        tts.preferences.genderFilter = gender
        selectedVoiceGender.value = gender
        val success = tts.setVoiceByGender(gender)
        if (success) {
            selectedVoiceName.value = tts.preferences.selectedVoiceName
            _toastMessage.value = "$gender voice selected."
        } else {
            _toastMessage.value = "No specific $gender voice package installed on device."
        }
    }

    fun testVoiceSpeech() {
        speakAloud("Voice calibration test. All JARVIS speech synthesizers are fully operational, sir.")
    }

    // Personal Memory DB Operations
    fun savePersonalMemory(key: String, value: String, category: String = "USER_PREFERENCE") {
        viewModelScope.launch {
            personalMemoryRepository.saveMemory(key, value, category)
            _toastMessage.value = "Memory saved: $key"
        }
    }

    fun deletePersonalMemory(id: Long) {
        viewModelScope.launch {
            personalMemoryRepository.deleteMemory(id)
            _toastMessage.value = "Memory deleted."
        }
    }

    fun clearAllPersonalMemories() {
        viewModelScope.launch {
            personalMemoryRepository.clearAll()
            _toastMessage.value = "All personal memories cleared."
        }
    }

    // Chat History DB Operations
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            chatHistoryRepository.deleteMessage(id)
            _toastMessage.value = "History record deleted."
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatHistoryRepository.clearHistory()
            _toastMessage.value = "All conversation history deleted."
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceActivationManager.destroy()
        tts.shutdown()
    }
}
