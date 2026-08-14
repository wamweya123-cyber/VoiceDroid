package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ParsedCommand
import com.example.ui.theme.AmberSecurity
import com.example.ui.theme.CoralBlocked
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecurity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommandConsoleScreen(
    isListening: Boolean,
    isProcessing: Boolean,
    spokenTranscript: String,
    latestCommand: ParsedCommand?,
    lastExecutionMessage: String,
    isContinuousListening: Boolean,
    isPreferOffline: Boolean,
    continuousStatusText: String,
    partialTranscript: String,
    onStartListening: () -> Unit,
    onStopAndProcess: (String?) -> Unit,
    onTranscriptChange: (String) -> Unit,
    onToggleContinuousListening: () -> Unit,
    onTogglePreferOffline: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Pulse animation for mic listening
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(isListening || isContinuousListening) {
        if (isListening || isContinuousListening) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("command_console_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Card
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_voice_remote_1785534547514),
                    contentDescription = "Futuristic Voice Remote",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD0F172A))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voice Remote Console",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Offline Continuous Voice & Hands-Free Navigation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Offline Continuous Listening Mode Master Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isContinuousListening) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isContinuousListening) CyanPrimary else Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isContinuousListening) EmeraldSecurity else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Continuous Voice Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isContinuousListening) EmeraldSecurity.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isContinuousListening) "ACTIVE LISTENING" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isContinuousListening) EmeraldSecurity else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = continuousStatusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Switch(
                            checked = isContinuousListening,
                            onCheckedChange = { onToggleContinuousListening() },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = CyanPrimary,
                                checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("continuous_mode_switch")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continuous Listening",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Switch(
                            checked = isPreferOffline,
                            onCheckedChange = { onTogglePreferOffline() },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = EmeraldSecurity,
                                checkedTrackColor = EmeraldSecurity.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("prefer_offline_switch")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prefer Offline",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (partialTranscript.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Live Stream: \"$partialTranscript\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Voice Command Listening Activation Hub
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isListening || isContinuousListening) "Listening for Voice Commands..." else "Tap Microphone or Speak Command",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isListening || isContinuousListening) CyanPrimary else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone Activation Button
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale.value)
                        .clip(CircleShape)
                        .background(
                            if (isListening || isContinuousListening) CyanPrimary else MaterialTheme.colorScheme.primaryContainer
                        )
                        .clickable {
                            if (isListening) {
                                onStopAndProcess(null)
                            } else {
                                onStartListening()
                            }
                        }
                        .testTag("mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening || isContinuousListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Voice Remote Mic",
                        tint = if (isListening || isContinuousListening) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcript Input Field
                OutlinedTextField(
                    value = spokenTranscript,
                    onValueChange = onTranscriptChange,
                    label = { Text("Spoken Command Transcript") },
                    placeholder = { Text("e.g. Next Tab, Open Security, Scroll Down") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_transcript_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary
                    ),
                    trailingIcon = {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            IconButton(onClick = { onStopAndProcess(spokenTranscript) }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Process Command",
                                    tint = CyanPrimary
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hands-Free Navigation Shortcuts
                Text(
                    text = "Hands-Free Voice Navigation Shortcuts:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val navPresets = listOf(
                        "Tab Apps",
                        "Tab Macros",
                        "Tab Security",
                        "Tab Settings",
                        "Next Screen",
                        "Go Back",
                        "Scroll Down",
                        "Mute Audio"
                    )

                    navPresets.forEach { preset ->
                        SuggestionChip(
                            onClick = {
                                onTranscriptChange(preset)
                                onStopAndProcess(preset)
                            },
                            label = { Text(preset, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        // Gemini AI Intent Parsing Output Card
        if (latestCommand != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (latestCommand.securityLevel == "HIGH_SECURITY") AmberSecurity else CyanPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = CyanPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Command Parser",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Security Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (latestCommand.securityLevel == "HIGH_SECURITY") AmberSecurity.copy(alpha = 0.2f)
                                    else EmeraldSecurity.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = latestCommand.securityLevel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (latestCommand.securityLevel == "HIGH_SECURITY") AmberSecurity else EmeraldSecurity,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Action Intent", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(latestCommand.action, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Target App", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(latestCommand.targetApp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Parameters", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(latestCommand.parameter, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Spoken Feedback: \"${latestCommand.ttsFeedback}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Status Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = lastExecutionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Interactive Remote Control Trackpad & Quick Gestures
        Card(
            shape = RoundedCornerShape(20.dp),
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
                Text(
                    text = "Interactive Remote Gesture Trackpad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RemoteButton(
                        icon = Icons.Default.Home,
                        label = "Home",
                        onClick = { onStopAndProcess("Go Home Screen") }
                    )
                    RemoteButton(
                        icon = Icons.Default.ArrowBack,
                        label = "Back",
                        onClick = { onStopAndProcess("Go Back") }
                    )
                    RemoteButton(
                        icon = Icons.Default.ArrowUpward,
                        label = "Scroll Up",
                        onClick = { onStopAndProcess("Scroll Up") }
                    )
                    RemoteButton(
                        icon = Icons.Default.ArrowDownward,
                        label = "Scroll Down",
                        onClick = { onStopAndProcess("Scroll Down") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RemoteButton(
                        icon = Icons.Default.VolumeMute,
                        label = "Mute Audio",
                        onClick = { onStopAndProcess("Mute Volume") }
                    )
                    RemoteButton(
                        icon = Icons.Default.Security,
                        label = "Read Screen",
                        onClick = { onStopAndProcess("Read Screen Text") }
                    )
                    RemoteButton(
                        icon = Icons.Default.Lock,
                        label = "Lockdown",
                        onClick = { onStopAndProcess("Lock Phone Immediately") }
                    )
                }
            }
        }
    }
}

@Composable
fun RemoteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
