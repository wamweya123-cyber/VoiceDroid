package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VoiceRemoteTab
import com.example.ui.VoiceRemoteViewModel
import com.example.ui.components.AccessibilitySettingsScreen
import com.example.ui.components.AppsControlScreen
import com.example.ui.components.CommandConsoleScreen
import com.example.ui.components.SecurityPinDialog
import com.example.ui.components.SecurityShieldScreen
import com.example.ui.components.VoiceMacrosScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VoiceRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceRemoteTheme {
                VoiceRemoteApp()
            }
        }
    }
}

@Composable
fun VoiceRemoteApp(viewModel: VoiceRemoteViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val spokenTranscript by viewModel.spokenTranscript.collectAsStateWithLifecycle()
    val latestCommand by viewModel.latestCommand.collectAsStateWithLifecycle()
    val lastExecutionMessage by viewModel.lastExecutionMessage.collectAsStateWithLifecycle()

    val securityAuditLogs by viewModel.securityAuditLogs.collectAsStateWithLifecycle()
    val voiceMacros by viewModel.voiceMacros.collectAsStateWithLifecycle()
    val appPermissionRules by viewModel.appPermissionRules.collectAsStateWithLifecycle()

    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    val enteredPin by viewModel.enteredPin.collectAsStateWithLifecycle()
    val showPinModal by viewModel.showPinModal.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()

    val ttsRate by viewModel.ttsRate.collectAsStateWithLifecycle()
    val voiceSensitivity by viewModel.voiceSensitivity.collectAsStateWithLifecycle()
    val remotePairingToken by viewModel.remotePairingToken.collectAsStateWithLifecycle()

    val isContinuousListening by viewModel.isContinuousListening.collectAsStateWithLifecycle()
    val isPreferOffline by viewModel.isPreferOffline.collectAsStateWithLifecycle()
    val continuousStatusText by viewModel.continuousStatusText.collectAsStateWithLifecycle()
    val partialTranscript by viewModel.partialTranscript.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == VoiceRemoteTab.CONSOLE,
                    onClick = { viewModel.selectTab(VoiceRemoteTab.CONSOLE) },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Console") },
                    label = { Text("Console") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_console_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == VoiceRemoteTab.APPS_CONTROL,
                    onClick = { viewModel.selectTab(VoiceRemoteTab.APPS_CONTROL) },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                    label = { Text("Apps") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_apps_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == VoiceRemoteTab.MACROS,
                    onClick = { viewModel.selectTab(VoiceRemoteTab.MACROS) },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Routines") },
                    label = { Text("Routines") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_macros_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == VoiceRemoteTab.SECURITY_SHIELD,
                    onClick = { viewModel.selectTab(VoiceRemoteTab.SECURITY_SHIELD) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Security") },
                    label = { Text("Security") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_security_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == VoiceRemoteTab.SETTINGS,
                    onClick = { viewModel.selectTab(VoiceRemoteTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                VoiceRemoteTab.CONSOLE -> {
                    CommandConsoleScreen(
                        isListening = isListening,
                        isProcessing = isProcessing,
                        spokenTranscript = spokenTranscript,
                        latestCommand = latestCommand,
                        lastExecutionMessage = lastExecutionMessage,
                        isContinuousListening = isContinuousListening,
                        isPreferOffline = isPreferOffline,
                        continuousStatusText = continuousStatusText,
                        partialTranscript = partialTranscript,
                        onStartListening = { viewModel.startListening() },
                        onStopAndProcess = { viewModel.stopListeningAndProcess(it) },
                        onTranscriptChange = { viewModel.setSpokenTranscriptText(it) },
                        onToggleContinuousListening = { viewModel.toggleContinuousListening() },
                        onTogglePreferOffline = { viewModel.togglePreferOffline() }
                    )
                }
                VoiceRemoteTab.APPS_CONTROL -> {
                    AppsControlScreen(
                        appRules = appPermissionRules,
                        onToggleRule = { viewModel.toggleAppPermissionRule(it) },
                        onTogglePin = { viewModel.toggleAppPinRequirement(it) },
                        onLaunchAppVoice = { viewModel.processVoiceCommand(it) }
                    )
                }
                VoiceRemoteTab.MACROS -> {
                    VoiceMacrosScreen(
                        macros = voiceMacros,
                        onCreateMacro = { title, trigger, actions ->
                            viewModel.createMacro(title, trigger, actions)
                        },
                        onToggleMacro = { viewModel.toggleMacro(it) },
                        onDeleteMacro = { viewModel.deleteMacro(it) },
                        onExecuteMacro = { viewModel.executeMacro(it) }
                    )
                }
                VoiceRemoteTab.SECURITY_SHIELD -> {
                    SecurityShieldScreen(
                        auditLogs = securityAuditLogs,
                        securityPin = securityPin,
                        pairingToken = remotePairingToken,
                        onClearLogs = { viewModel.clearAuditLogs() },
                        onTestPinPrompt = {
                            viewModel.processVoiceCommand("Open Secure Mobile Banking App")
                        }
                    )
                }
                VoiceRemoteTab.SETTINGS -> {
                    AccessibilitySettingsScreen(
                        ttsRate = ttsRate,
                        voiceSensitivity = voiceSensitivity,
                        securityPin = securityPin,
                        pairingToken = remotePairingToken,
                        isContinuousListening = isContinuousListening,
                        isPreferOffline = isPreferOffline,
                        onTtsRateChange = { viewModel.updateTtsRate(it) },
                        onSensitivityChange = { viewModel.updateVoiceSensitivity(it) },
                        onUpdatePin = { viewModel.updateSecurityPin(it) },
                        onTriggerLockdown = {
                            viewModel.processVoiceCommand("Lock Phone Immediately")
                        },
                        onToggleContinuousListening = { viewModel.toggleContinuousListening() },
                        onTogglePreferOffline = { viewModel.togglePreferOffline() }
                    )
                }
            }

            if (showPinModal) {
                SecurityPinDialog(
                    enteredPin = enteredPin,
                    isError = pinError,
                    onDigitClick = { viewModel.updateEnteredPin(it) },
                    onDeleteClick = { viewModel.deletePinDigit() },
                    onConfirmClick = { viewModel.verifyPinAndExecute(enteredPin) },
                    onDismiss = { viewModel.dismissPinModal() }
                )
            }
        }
    }
}
