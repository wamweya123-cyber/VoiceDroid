package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPermissionRule
import com.example.data.SecurityAuditLog
import com.example.data.VoiceMacro
import com.example.data.VoiceRemoteDatabase
import com.example.data.VoiceRemoteRepository
import com.example.network.GeminiClient
import com.example.service.ContinuousVoiceListener
import com.example.service.VoiceExecutionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class VoiceRemoteTab {
    CONSOLE,
    APPS_CONTROL,
    MACROS,
    SECURITY_SHIELD,
    SETTINGS
}

data class ParsedCommand(
    val action: String = "OPEN_APP",
    val targetApp: String = "Settings",
    val parameter: String = "Foreground",
    val securityLevel: String = "NORMAL",
    val ttsFeedback: String = "Ready"
)

class VoiceRemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VoiceRemoteRepository
    private val voiceEngine: VoiceExecutionEngine = VoiceExecutionEngine(application)

    private val _isContinuousListening = MutableStateFlow(false)
    val isContinuousListening: StateFlow<Boolean> = _isContinuousListening.asStateFlow()

    private val _isPreferOffline = MutableStateFlow(true)
    val isPreferOffline: StateFlow<Boolean> = _isPreferOffline.asStateFlow()

    private val _continuousStatusText = MutableStateFlow("Hands-Free Mode Ready")
    val continuousStatusText: StateFlow<String> = _continuousStatusText.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val continuousVoiceListener: ContinuousVoiceListener by lazy {
        ContinuousVoiceListener(
            context = application,
            onFinalResult = { text ->
                _spokenTranscript.value = text
                _partialTranscript.value = ""
                processVoiceCommand(text)
            },
            onPartialResult = { partial ->
                _partialTranscript.value = partial
            },
            onErrorState = { err ->
                _continuousStatusText.value = err
            },
            onStatusChange = { status ->
                _continuousStatusText.value = status
            }
        )
    }

    init {
        val database = VoiceRemoteDatabase.getDatabase(application)
        repository = VoiceRemoteRepository(database)
        viewModelScope.launch {
            repository.insertDefaultRulesIfEmpty()
        }
    }

    val securityAuditLogs: StateFlow<List<SecurityAuditLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceMacros: StateFlow<List<VoiceMacro>> = repository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appPermissionRules: StateFlow<List<AppPermissionRule>> = repository.allAppRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(VoiceRemoteTab.CONSOLE)
    val selectedTab: StateFlow<VoiceRemoteTab> = _selectedTab.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenTranscript = MutableStateFlow("")
    val spokenTranscript: StateFlow<String> = _spokenTranscript.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _latestCommand = MutableStateFlow<ParsedCommand?>(null)
    val latestCommand: StateFlow<ParsedCommand?> = _latestCommand.asStateFlow()

    private val _lastExecutionMessage = MutableStateFlow("System Ready for Remote Voice Commands")
    val lastExecutionMessage: StateFlow<String> = _lastExecutionMessage.asStateFlow()

    // Security & Access PIN
    private val _securityPin = MutableStateFlow("1234")
    val securityPin: StateFlow<String> = _securityPin.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _showPinModal = MutableStateFlow(false)
    val showPinModal: StateFlow<Boolean> = _showPinModal.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    private var pendingSecurityCommand: ParsedCommand? = null

    // Accessibility & TTS Settings
    private val _ttsRate = MutableStateFlow(1.0f)
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private val _voiceSensitivity = MutableStateFlow(0.85f)
    val voiceSensitivity: StateFlow<Float> = _voiceSensitivity.asStateFlow()

    private val _remotePairingToken = MutableStateFlow("VREM-9823-SEC")
    val remotePairingToken: StateFlow<String> = _remotePairingToken.asStateFlow()

    fun selectTab(tab: VoiceRemoteTab) {
        _selectedTab.value = tab
    }

    fun startListening() {
        _isListening.value = true
        _spokenTranscript.value = "Listening..."
    }

    fun stopListeningAndProcess(sampleVoiceInput: String? = null) {
        _isListening.value = false
        val textToProcess = sampleVoiceInput ?: _spokenTranscript.value.ifBlank { "Open Settings" }
        _spokenTranscript.value = textToProcess
        processVoiceCommand(textToProcess)
    }

    fun setSpokenTranscriptText(text: String) {
        _spokenTranscript.value = text
    }

    fun processVoiceCommand(commandText: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val jsonResult = GeminiClient.analyzeVoiceCommand(commandText, preferOffline = _isPreferOffline.value)
            _isProcessing.value = false

            val parsed = try {
                val obj = JSONObject(jsonResult)
                ParsedCommand(
                    action = obj.optString("action", "OPEN_APP"),
                    targetApp = obj.optString("targetApp", "Settings"),
                    parameter = obj.optString("parameter", "Foreground"),
                    securityLevel = obj.optString("securityLevel", "NORMAL"),
                    ttsFeedback = obj.optString("ttsFeedback", "Executing command")
                )
            } catch (e: Exception) {
                ParsedCommand(
                    action = "OPEN_APP",
                    targetApp = "Settings",
                    parameter = "Foreground",
                    securityLevel = "NORMAL",
                    ttsFeedback = "Executing: $commandText"
                )
            }

            _latestCommand.value = parsed

            // Check if macro matches trigger
            val currentMacros = voiceMacros.value
            val matchedMacro = currentMacros.find {
                commandText.lowercase().contains(it.voiceTrigger.lowercase()) && it.isEnabled
            }

            if (matchedMacro != null) {
                executeMacro(matchedMacro)
                return@launch
            }

            if (parsed.securityLevel == "HIGH_SECURITY") {
                // Requires PIN authorization
                pendingSecurityCommand = parsed
                _showPinModal.value = true
                _lastExecutionMessage.value = "High Security Action Detected! PIN Required."
                voiceEngine.speak("High Security Action requested. Please enter your voice authorization PIN.")
                
                repository.insertLog(
                    SecurityAuditLog(
                        voiceCommand = commandText,
                        detectedIntent = parsed.action,
                        targetApp = parsed.targetApp,
                        status = "PIN_REQUIRED",
                        details = "Command flagged for security verification."
                    )
                )
            } else {
                executeParsedCommand(parsed, commandText)
            }
        }
    }

    private fun executeParsedCommand(command: ParsedCommand, rawInput: String) {
        if (command.action == "NAVIGATE_TAB") {
            val tabParam = command.parameter.uppercase()
            when {
                tabParam.contains("CONSOLE") -> selectTab(VoiceRemoteTab.CONSOLE)
                tabParam.contains("APPS") -> selectTab(VoiceRemoteTab.APPS_CONTROL)
                tabParam.contains("MACROS") -> selectTab(VoiceRemoteTab.MACROS)
                tabParam.contains("SECURITY") -> selectTab(VoiceRemoteTab.SECURITY_SHIELD)
                tabParam.contains("SETTINGS") -> selectTab(VoiceRemoteTab.SETTINGS)
                tabParam.contains("NEXT") -> {
                    val tabs = VoiceRemoteTab.values()
                    val nextIdx = (_selectedTab.value.ordinal + 1) % tabs.size
                    selectTab(tabs[nextIdx])
                }
                tabParam.contains("PREV") -> {
                    val tabs = VoiceRemoteTab.values()
                    val prevIdx = if (_selectedTab.value.ordinal == 0) tabs.size - 1 else _selectedTab.value.ordinal - 1
                    selectTab(tabs[prevIdx])
                }
            }
        }

        val result = if (command.action == "NAVIGATE_TAB") {
            "Hands-Free Voice Navigation to ${command.targetApp}"
        } else {
            voiceEngine.executeSystemAction(command.action, command.targetApp, command.parameter)
        }

        voiceEngine.speak(command.ttsFeedback, _ttsRate.value)
        _lastExecutionMessage.value = result

        viewModelScope.launch {
            repository.insertLog(
                SecurityAuditLog(
                    voiceCommand = rawInput,
                    detectedIntent = command.action,
                    targetApp = command.targetApp,
                    status = "EXECUTED",
                    details = result
                )
            )
        }
    }

    fun verifyPinAndExecute(pinInput: String) {
        if (pinInput == _securityPin.value) {
            _showPinModal.value = false
            _pinError.value = false
            _enteredPin.value = ""
            val cmd = pendingSecurityCommand
            if (cmd != null) {
                executeParsedCommand(cmd, _spokenTranscript.value)
                pendingSecurityCommand = null
            }
        } else {
            _pinError.value = true
            voiceEngine.speak("Incorrect PIN. Remote access denied.")
            viewModelScope.launch {
                repository.insertLog(
                    SecurityAuditLog(
                        voiceCommand = _spokenTranscript.value,
                        detectedIntent = pendingSecurityCommand?.action ?: "UNKNOWN",
                        targetApp = pendingSecurityCommand?.targetApp ?: "UNKNOWN",
                        status = "BLOCKED_SECURITY",
                        details = "Failed Voice PIN verification attempt."
                    )
                )
            }
        }
    }

    fun dismissPinModal() {
        _showPinModal.value = false
        _enteredPin.value = ""
        _pinError.value = false
        pendingSecurityCommand = null
    }

    fun updateEnteredPin(digit: String) {
        if (_enteredPin.value.length < 4) {
            _enteredPin.value += digit
            _pinError.value = false
        }
    }

    fun deletePinDigit() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    fun createMacro(title: String, trigger: String, actions: String) {
        viewModelScope.launch {
            repository.insertMacro(
                VoiceMacro(
                    title = title,
                    voiceTrigger = trigger,
                    actionsList = actions,
                    isEnabled = true
                )
            )
            voiceEngine.speak("Created voice macro routine $title")
        }
    }

    fun toggleMacro(macro: VoiceMacro) {
        viewModelScope.launch {
            repository.updateMacro(macro.copy(isEnabled = !macro.isEnabled))
        }
    }

    fun deleteMacro(macroId: Long) {
        viewModelScope.launch {
            repository.deleteMacro(macroId)
        }
    }

    fun executeMacro(macro: VoiceMacro) {
        _lastExecutionMessage.value = "Executing Voice Routine: ${macro.title}"
        voiceEngine.speak("Starting ${macro.title} routine")
        
        viewModelScope.launch {
            val actions = macro.actionsList.split("|")
            for (act in actions) {
                val trimmed = act.trim()
                if (trimmed.isNotEmpty()) {
                    voiceEngine.executeSystemAction("CHANGE_SETTING", "System", trimmed)
                }
            }
            repository.insertLog(
                SecurityAuditLog(
                    voiceCommand = macro.voiceTrigger,
                    detectedIntent = "ROUTINE_MACRO",
                    targetApp = macro.title,
                    status = "EXECUTED",
                    details = "Completed routine actions: ${macro.actionsList}"
                )
            )
        }
    }

    fun toggleAppPermissionRule(rule: AppPermissionRule) {
        viewModelScope.launch {
            repository.insertOrUpdateAppRule(rule.copy(isRemoteControlAllowed = !rule.isRemoteControlAllowed))
        }
    }

    fun toggleAppPinRequirement(rule: AppPermissionRule) {
        viewModelScope.launch {
            repository.insertOrUpdateAppRule(rule.copy(requiresVoicePin = !rule.requiresVoicePin))
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun updateTtsRate(rate: Float) {
        _ttsRate.value = rate
    }

    fun updateVoiceSensitivity(sens: Float) {
        _voiceSensitivity.value = sens
        continuousVoiceListener.setSensitivity(sens)
    }

    fun updateSecurityPin(newPin: String) {
        if (newPin.length == 4) {
            _securityPin.value = newPin
            voiceEngine.speak("Voice Security PIN updated successfully.")
        }
    }

    fun toggleContinuousListening(enabled: Boolean? = null) {
        val targetState = enabled ?: !_isContinuousListening.value
        _isContinuousListening.value = targetState
        if (targetState) {
            continuousVoiceListener.startContinuousListening()
            voiceEngine.speak("Offline continuous voice mode activated. Hands-free listening live.")
        } else {
            continuousVoiceListener.stopListening()
            voiceEngine.speak("Continuous listening mode paused.")
        }
    }

    fun togglePreferOffline(enabled: Boolean? = null) {
        val targetState = enabled ?: !_isPreferOffline.value
        _isPreferOffline.value = targetState
        val msg = if (targetState) "Offline Local Command Engine enabled." else "Online Hybrid Speech Engine enabled."
        voiceEngine.speak(msg)
    }

    override fun onCleared() {
        super.onCleared()
        continuousVoiceListener.destroy()
        voiceEngine.shutdown()
    }
}
