package com.example.arlo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.model.AuditEntry
import com.example.arlo.model.Permissions
import com.example.arlo.ui.theme.*

@Composable
fun PrivacyScreen(
    permissions: Permissions,
    auditTrail: List<AuditEntry>,
    onUpdatePermission: (String, Boolean) -> Unit,
    onPauseAll: () -> Unit,
    onLockArlo: () -> Unit,
    onChangePassphrase: (String, String) -> Result<Unit>,
    onExportBackup: () -> String?,
    onResetVault: () -> Unit
) {
    val context = LocalContext.current
    var showChangePassDialog by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var changePassError by remember { mutableStateOf<String?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupContent by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    val permissionItems = listOf(
        Triple("calendar", "Calendar and reminders", permissions.calendar),
        Triple("location", "Location signals", permissions.location),
        Triple("notifications", "Notifications", permissions.notifications),
        Triple("files", "Files and notes", permissions.files),
        Triple("microphone", "Microphone", permissions.microphone),
        Triple("camera", "Camera", permissions.camera),
        Triple("accessibility", "Accessibility signals", permissions.accessibility),
        Triple("cloudAI", "Optional cloud AI", permissions.cloudAI)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Topbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONTROL ROOM",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary
                    )
                    Text(
                        text = "Your privacy",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ArloTextPrimary
                    )
                }
                OutlinedButton(
                    onClick = onPauseAll,
                    modifier = Modifier.testTag("pause_all_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloDanger),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloDanger.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause all")
                }
            }
        }

        // Philosophy Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ArloPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ArloPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "You are in charge.",
                            style = MaterialTheme.typography.titleLarge,
                            color = ArloTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arlo uses one simple encrypted vault, while each external source remains separately controlled.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloTextSecondary
                        )
                    }
                }
            }
        }

        // Permissions List Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Source & Device Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    permissionItems.forEach { (key, label, isEnabled) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = ArloTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (key == "cloudAI")
                                        "Off by default; cloud requests require a separate future consent flow."
                                    else
                                        "Only used after you enable this category.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextMuted
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { onUpdatePermission(key, it) },
                                modifier = Modifier.testTag("perm_switch_$key"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArloOnPrimary,
                                    checkedTrackColor = ArloPrimary,
                                    uncheckedThumbColor = ArloTextMuted,
                                    uncheckedTrackColor = ArloDarkSurfaceVariant
                                )
                            )
                        }
                        if (key != permissionItems.last().first) {
                            HorizontalDivider(color = ArloBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Vault Hardening & Security Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Vault Protection",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vault: AES-256-GCM encrypted. Keys derived via PBKDF2 (600,000 iterations).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangePassDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                        ) {
                            Text("Change passphrase", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val backup = onExportBackup()
                                if (backup != null) {
                                    backupContent = backup
                                    showBackupDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export backup", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLockArlo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_arlo_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloSecondaryContainer,
                            contentColor = ArloDanger
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloDanger.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Arlo", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset vault / delete local data", color = ArloDanger.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }
        }

        // Audit Trail Section
        item {
            Text(
                text = "Security Audit Trail (${auditTrail.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary
            )
        }

        if (auditTrail.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No audit events recorded yet.", color = ArloTextMuted)
                    }
                }
            }
        } else {
            items(auditTrail.take(15)) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArloDarkSurface, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.action,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextPrimary
                    )
                    Text(
                        text = entry.at.take(19).replace('T', ' '),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextMuted
                    )
                }
            }
        }
    }

    if (showChangePassDialog) {
        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            title = { Text("Change Vault Passphrase") },
            text = {
                Column {
                    if (changePassError != null) {
                        Text(changePassError!!, color = ArloDanger, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New passphrase (min 12 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = onChangePassphrase(oldPass, newPass)
                        res.onSuccess {
                            showChangePassDialog = false
                            oldPass = ""
                            newPass = ""
                            changePassError = null
                            Toast.makeText(context, "Passphrase updated successfully", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            changePassError = it.message ?: "Failed to update passphrase"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Encrypted Vault Backup") },
            text = {
                Column {
                    Text(
                        "This encrypted envelope contains your data encrypted with your passphrase. Copy it to a safe place.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = ArloDarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = backupContent.take(300) + if (backupContent.length > 300) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextMuted,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Arlo Vault Backup", backupContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Copy to clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Delete Vault?") },
            text = {
                Text(
                    "Are you sure? This will delete your local encrypted vault and all goals, notes, and tasks permanently.",
                    color = ArloTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetVault()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloDanger)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
