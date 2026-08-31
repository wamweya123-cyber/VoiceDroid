package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralBlocked
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecurity

data class VoiceCommandInfo(
    val trigger: String,
    val category: String,
    val description: String,
    val example: String,
    val isSecurityProtected: Boolean = false,
    val isOfflineSupported: Boolean = true
)

@Composable
fun AccessibilitySettingsScreen(
    ttsRate: Float,
    voiceSensitivity: Float,
    securityPin: String,
    pairingToken: String,
    isContinuousListening: Boolean = false,
    isPreferOffline: Boolean = true,
    onTtsRateChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onUpdatePin: (String) -> Unit,
    onTriggerLockdown: () -> Unit,
    onToggleContinuousListening: () -> Unit = {},
    onTogglePreferOffline: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var pinInput by remember { mutableStateOf(securityPin) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    // Voice Commands Directory State
    var isCommandsExpanded by remember { mutableStateOf(false) }
    var showCommandsModal by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val allVoiceCommands = remember {
        listOf(
            VoiceCommandInfo(
                trigger = "Open Console / Tab Console",
                category = "Navigation",
                description = "Switches hands-free to the main Command Console with active voice visualizer.",
                example = "Go to console"
            ),
            VoiceCommandInfo(
                trigger = "Open Apps / Tab Apps",
                category = "Navigation",
                description = "Navigates to the phone application manager and quick launch grid.",
                example = "Open Apps"
            ),
            VoiceCommandInfo(
                trigger = "Open Macros / Tab Macros",
                category = "Navigation",
                description = "Opens the automation routines and multi-step voice scripts screen.",
                example = "Tab Macros"
            ),
            VoiceCommandInfo(
                trigger = "Open Security / Tab Security",
                category = "Navigation",
                description = "Views real-time access audit logs and security shield status.",
                example = "Open Security"
            ),
            VoiceCommandInfo(
                trigger = "Open Settings / Tab Settings",
                category = "Navigation",
                description = "Accesses TTS speed, microphone sensitivity, and PIN configuration.",
                example = "Open Settings"
            ),
            VoiceCommandInfo(
                trigger = "Next Tab / Previous Tab",
                category = "Navigation",
                description = "Hands-free cyclic navigation across tabs without touching the screen.",
                example = "Next screen"
            ),
            VoiceCommandInfo(
                trigger = "Scroll Down / Page Down",
                category = "System",
                description = "Performs an automatic downward scroll gesture on the active screen.",
                example = "Scroll down"
            ),
            VoiceCommandInfo(
                trigger = "Scroll Up / Page Up",
                category = "System",
                description = "Performs an automatic upward scroll gesture on the active screen.",
                example = "Scroll up"
            ),
            VoiceCommandInfo(
                trigger = "Set Volume [0-100]%",
                category = "System",
                description = "Dynamically adjusts system master audio volume to any percentage level.",
                example = "Set volume to 50%"
            ),
            VoiceCommandInfo(
                trigger = "Mute / Unmute",
                category = "System",
                description = "Instantly silences or restores the device audio speaker output.",
                example = "Mute audio"
            ),
            VoiceCommandInfo(
                trigger = "Turn on / off Wi-Fi",
                category = "System",
                description = "Toggles Wi-Fi or Bluetooth connectivity switches.",
                example = "Turn Wi-Fi on"
            ),
            VoiceCommandInfo(
                trigger = "Read Screen / Read Aloud",
                category = "System",
                description = "Accessibility screen reader scans and speaks displayed text aloud.",
                example = "Read screen aloud"
            ),
            VoiceCommandInfo(
                trigger = "Open [App Name]",
                category = "Apps",
                description = "Launches any installed application directly into foreground.",
                example = "Open YouTube"
            ),
            VoiceCommandInfo(
                trigger = "Play / Pause / Next Track",
                category = "Apps",
                description = "Controls active media and music playback transport hands-free.",
                example = "Pause music"
            ),
            VoiceCommandInfo(
                trigger = "Launch Camera",
                category = "Apps",
                description = "Opens camera viewfinder for hands-free photo or video capture.",
                example = "Launch Camera"
            ),
            VoiceCommandInfo(
                trigger = "Lock Phone / Secure Device",
                category = "Security",
                description = "Immediately turns off screen and activates biometric/PIN lock.",
                example = "Lock phone now",
                isSecurityProtected = true
            ),
            VoiceCommandInfo(
                trigger = "Open Banking / Reset Password",
                category = "Security",
                description = "High-security protected actions requiring voice security PIN authorization.",
                example = "Open Bank app",
                isSecurityProtected = true
            ),
            VoiceCommandInfo(
                trigger = "Emergency Lockdown",
                category = "Security",
                description = "Immediately terminates remote voice listeners and enforces phone security lock.",
                example = "Trigger Lockdown",
                isSecurityProtected = true
            ),
            VoiceCommandInfo(
                trigger = "Good Morning Routine",
                category = "Macros",
                description = "Executes custom multi-step macro: unmutes volume, speaks briefing, launches news.",
                example = "Start morning routine"
            ),
            VoiceCommandInfo(
                trigger = "Bedtime Mode",
                category = "Macros",
                description = "Mutes volume, turns off wireless radios, and secures screen.",
                example = "Bedtime mode"
            )
        )
    }

    val categories = remember { listOf("All", "Navigation", "System", "Apps", "Security", "Macros") }

    val filteredCommands = remember(searchQuery, selectedCategory, allVoiceCommands) {
        allVoiceCommands.filter { cmd ->
            val matchesCategory = (selectedCategory == "All") || (cmd.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    cmd.trigger.contains(searchQuery, ignoreCase = true) ||
                    cmd.description.contains(searchQuery, ignoreCase = true) ||
                    cmd.example.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("accessibility_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prominent Download APK Hero Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(2.dp, CyanPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("download_apk_hero_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = "Android APK",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Download APK",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v1.0.0 • Offline Ready Package",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSecurity.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSecurity,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Directly export and install the Voice Remote Android application onto your physical phone for full offline hands-free control.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Prominent Download Button
                Button(
                    onClick = {
                        showDownloadDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("download_apk_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download APK Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DOWNLOAD APK FILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Guide Button
                OutlinedButton(
                    onClick = {
                        showInstructionsDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("install_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Installation Guide",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "How to Install & Sideload on Phone",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Accessibility & Voice Controls",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Adjust Text-To-Speech spoken feedback speed, microphone voice sensitivity, and security credentials for remote operation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Available Voice Commands & Controls Directory (Expandable Section)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, if (isCommandsExpanded) CyanPrimary else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("available_voice_commands_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row (Clickable to toggle expansion)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isCommandsExpanded = !isCommandsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Commands",
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Available Voice Commands",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${allVoiceCommands.size} Supported Hands-Free Controls",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCommandsExpanded) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCommandsExpanded) "Expanded" else "Tap to View",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCommandsExpanded) CyanPrimary else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { isCommandsExpanded = !isCommandsExpanded },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("toggle_voice_commands_expand_button")
                        ) {
                            Icon(
                                imageVector = if (isCommandsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isCommandsExpanded) "Collapse" else "Expand",
                                tint = CyanPrimary
                            )
                        }
                    }
                }

                // Collapsed Summary Preview
                if (!isCommandsExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse complete categorized voice controls for tabs navigation, scrolling gestures, system volume, apps launch, and security locks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Command Chips
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("\"Open Console\"", "\"Scroll Down\"", "\"Set Volume 50%\"", "\"Lock Phone\"").forEach { phrase ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = phrase,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isCommandsExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary.copy(alpha = 0.2f),
                                contentColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("expand_commands_section_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Expand Directory", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCommandsModal = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("open_commands_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modal View", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Expanded Section Content with AnimatedVisibility
                AnimatedVisibility(
                    visible = isCommandsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Filter Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search voice commands...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 52.dp)
                                .testTag("search_voice_commands_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips Filter
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                val count = if (cat == "All") allVoiceCommands.size else allVoiceCommands.count { it.category.equals(cat, ignoreCase = true) }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text("$cat ($count)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary,
                                        selectedLabelColor = Color.Black
                                    ),
                                    modifier = Modifier.testTag("filter_chip_$cat")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Commands List
                        if (filteredCommands.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No commands matching \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredCommands.forEach { command ->
                                    VoiceCommandCardItem(
                                        command = command,
                                        onCopyExample = { phrase ->
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Voice Command", phrase)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied: \"$phrase\"", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Modal launcher and collapse button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCommandsModal = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_full_modal_guide_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full Modal View", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { isCommandsExpanded = false },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("collapse_commands_section_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Collapse", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Offline Continuous Listening Settings Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldSecurity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Continuous Voice Recognition", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Continuous Voice Capture", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Keep microphone actively listening in background for hands-free navigation", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isContinuousListening,
                        onCheckedChange = { onToggleContinuousListening() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanPrimary,
                            checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prefer Local Offline Speech Engine", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Zero network latency parsing using built-in offline rules", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isPreferOffline,
                        onCheckedChange = { onTogglePreferOffline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldSecurity,
                            checkedTrackColor = EmeraldSecurity.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Continuous Listening Sensitivity & Noise Gate Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continuous Listening Sensitivity",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(voiceSensitivity * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (voiceSensitivity >= 0.8f) "High Noise Suppression (Strict - Reduces False Triggers)"
                               else if (voiceSensitivity >= 0.6f) "Balanced Sensitivity"
                               else "High Sensitivity (Captures Quiet Commands)",
                        fontSize = 12.sp,
                        color = if (voiceSensitivity >= 0.8f) EmeraldSecurity else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = voiceSensitivity,
                        onValueChange = onSensitivityChange,
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary
                        ),
                        modifier = Modifier.testTag("continuous_sensitivity_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low (0.1)", fontSize = 10.sp, color = Color.Gray)
                        Text("Balanced (0.5)", fontSize = 10.sp, color = Color.Gray)
                        Text("Strict (1.0)", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // TTS Speech Rate Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accessibility Voice Rate (TTS)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Speech Rate: String.format(\"%.2fx\", ttsRate)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = ttsRate,
                    onValueChange = onTtsRateChange,
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Slow (0.5x)", fontSize = 11.sp, color = Color.Gray)
                    Text("Normal (1.0x)", fontSize = 11.sp, color = Color.Gray)
                    Text("Fast (2.0x)", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Voice Microphone Sensitivity Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Command Recognition Sensitivity", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sensitivity: ${(voiceSensitivity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = voiceSensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary
                    )
                )
            }
        }

        // Voice PIN Configuration Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Security PIN Setup", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinInput = it
                        }
                    },
                    label = { Text("4-Digit Security PIN") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onUpdatePin(pinInput) },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update PIN")
                }
            }
        }

        // Emergency Lockdown
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CoralBlocked.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CoralBlocked)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Lockdown Protocol", fontWeight = FontWeight.Bold, color = CoralBlocked)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Instantly sever all remote voice connections, lock screen access, and mute device outputs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTriggerLockdown,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralBlocked),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_lockdown_button")
                ) {
                    Text("TRIGGER IMMEDIATE LOCKDOWN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Download APK Package Dialog
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Android APK Package Export",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "The standalone Android Package (APK) has been compiled and is ready for installation on your Android phone.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("File Name:", fontSize = 12.sp, color = Color.Gray)
                                Text("app-debug.apk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Version:", fontSize = 12.sp, color = Color.Gray)
                                Text("1.0.0 (Build 1)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Offline Voice Engine:", fontSize = 12.sp, color = Color.Gray)
                                Text("Integrated (Zero Latency)", fontSize = 12.sp, color = EmeraldSecurity, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Architecture:", fontSize = 12.sp, color = Color.Gray)
                                Text("Universal (ARM64 / x86_64)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "APK Download Info",
                                    "Voice Remote APK Package: app-debug.apk (v1.0.0). Built with offline voice control engine and hands-free navigation."
                                )
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "APK Package details copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Info", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Download & Install Voice Remote Android APK (v1.0.0) with offline hands-free voice control.")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Voice Remote APK")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadDialog = false
                        Toast.makeText(context, "APK download package selected.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDownloadDialog = false
                    showInstructionsDialog = true
                }) {
                    Text("View Install Guide")
                }
            }
        )
    }

    // Step-by-Step Installation Guide Dialog
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "How to Install APK on Phone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Follow these 4 simple steps to install the APK onto your Android phone:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val steps = listOf(
                        "1. Download or transfer 'app-debug.apk' to your phone's Downloads folder.",
                        "2. Open your phone's File Manager or Downloads app and tap 'app-debug.apk'.",
                        "3. When prompted, toggle 'Allow from this source' under 'Install Unknown Apps'.",
                        "4. Tap 'Install' and launch 'Voice Remote'. Grant Microphone & Accessibility permissions for continuous offline voice control."
                    )

                    steps.forEach { step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSecurity,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInstructionsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // Modal Dialog: Comprehensive Available Voice Commands Directory
    if (showCommandsModal) {
        AlertDialog(
            onDismissRequest = { showCommandsModal = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Supported Voice Commands",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Complete directory of hands-free spoken triggers and system control capabilities:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Search input inside modal
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter commands...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 48.dp)
                    )

                    // Filter chips row
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Scrollable list of commands
                    val modalListScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(modalListScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filteredCommands.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No voice commands found for \"$searchQuery\"", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            filteredCommands.forEach { command ->
                                VoiceCommandCardItem(
                                    command = command,
                                    onCopyExample = { phrase ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Voice Command", phrase)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied: \"$phrase\"", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCommandsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Close Guide")
                }
            }
        )
    }
}

@Composable
fun VoiceCommandCardItem(
    command: VoiceCommandInfo,
    onCopyExample: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (command.isSecurityProtected) CoralBlocked.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_command_card_${command.category}_${command.trigger.take(8)}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (command.category) {
                            "Navigation" -> Icons.Default.Navigation
                            "System" -> Icons.Default.TouchApp
                            "Apps" -> Icons.Default.Apps
                            "Security" -> Icons.Default.Lock
                            else -> Icons.Default.RecordVoiceOver
                        },
                        contentDescription = null,
                        tint = if (command.isSecurityProtected) CoralBlocked else CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = command.trigger,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (command.isSecurityProtected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CoralBlocked.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PIN REQUIRED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralBlocked
                            )
                        }
                    } else if (command.isOfflineSupported) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldSecurity.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFLINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSecurity
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = command.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Example spoken phrase bubble with copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onCopyExample(command.example) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Say:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"${command.example}\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy command phrase",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
