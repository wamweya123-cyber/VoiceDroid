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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecurityAuditLog
import com.example.ui.theme.AmberSecurity
import com.example.ui.theme.CoralBlocked
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecurity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecurityShieldScreen(
    auditLogs: List<SecurityAuditLog>,
    securityPin: String,
    pairingToken: String,
    onClearLogs: () -> Unit,
    onTestPinPrompt: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("security_shield_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldSecurity.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldSecurity,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Voice Remote Security Shield",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AES-256 Voice Token Encryption Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecurity,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSecurity)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ACTIVE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Remote Pairing Key", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(pairingToken, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }

                    Column {
                        Text("Voice Auth PIN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("•••• ($securityPin)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onTestPinPrompt,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Auth PIN", fontSize = 12.sp)
                    }
                }
            }
        }

        // Audit Logs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security Audit Logs (${auditLogs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (auditLogs.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearLogs,
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Logs", fontSize = 12.sp)
                }
            }
        }

        if (auditLogs.isEmpty()) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSecurity,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Security Violation Logs", fontWeight = FontWeight.Bold)
                    Text(
                        "Every executed or blocked remote voice action is securely logged here in local Room persistence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    AuditLogItemCard(log = log, formattedTime = dateFormat.format(Date(log.timestamp)))
                }
            }
        }
    }
}

@Composable
fun AuditLogItemCard(log: SecurityAuditLog, formattedTime: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (log.status) {
                        "EXECUTED" -> Icons.Default.CheckCircle
                        "PIN_REQUIRED" -> Icons.Default.Lock
                        else -> Icons.Default.Block
                    },
                    contentDescription = null,
                    tint = when (log.status) {
                        "EXECUTED" -> EmeraldSecurity
                        "PIN_REQUIRED" -> AmberSecurity
                        else -> CoralBlocked
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "\"${log.voiceCommand}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Intent: ${log.detectedIntent} • Target: ${log.targetApp}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${log.details} • $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (log.status) {
                            "EXECUTED" -> EmeraldSecurity.copy(alpha = 0.2f)
                            "PIN_REQUIRED" -> AmberSecurity.copy(alpha = 0.2f)
                            else -> CoralBlocked.copy(alpha = 0.2f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (log.status) {
                        "EXECUTED" -> EmeraldSecurity
                        "PIN_REQUIRED" -> AmberSecurity
                        else -> CoralBlocked
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
