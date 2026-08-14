package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralBlocked
import com.example.ui.theme.CyanPrimary

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.ui.theme.EmeraldSecurity

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
    val scrollState = rememberScrollState()
    var pinInput by remember { mutableStateOf(securityPin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("accessibility_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
}
