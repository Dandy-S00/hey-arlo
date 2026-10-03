package com.example.arlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.model.AdaptiveTrial
import com.example.arlo.model.Memory
import com.example.arlo.model.MemoryEntry
import com.example.arlo.ui.theme.*

@Composable
fun MemoryScreen(
    memory: Memory,
    onStartTrial: (String) -> Unit,
    onTrialFeedback: (String, Boolean) -> Unit,
    onAddMemoryEntry: (String, String, String) -> Unit,
    onRevokeSource: (String) -> Unit,
    onDeleteMemoryEntry: (String) -> Unit
) {
    var styleAdjustment by remember { mutableStateOf("") }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var newEntrySummary by remember { mutableStateOf("") }
    var newEntrySourceLabel by remember { mutableStateOf("User input") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "ADAPTIVE HELP",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )
                Text(
                    text = "Memory",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArloTextPrimary
                )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Arlo learns gently.",
                        style = MaterialTheme.typography.titleLarge,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Memory is encrypted locally. Arlo may try low-risk communication changes, but permanent preferences require your approval.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorderHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CURRENT PREFERENCES",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Style: ${memory.preferences.communicationStyle}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tone: ${memory.preferences.tone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextSecondary
                            )
                            Text(
                                text = "Feedback: ${memory.preferences.feedbackPreference}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Propose adaptation form
                    OutlinedTextField(
                        value = styleAdjustment,
                        onValueChange = { styleAdjustment = it },
                        placeholder = { Text("Try a style adjustment, e.g. shorter answers") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_trial_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (styleAdjustment.isNotBlank()) {
                                onStartTrial(styleAdjustment)
                                styleAdjustment = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_trial_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start a 7-day trial", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Adaptive Trials Section
        item {
            Text(
                text = "Adaptive trials (${memory.learning.pendingTrials.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary
            )
        }

        if (memory.learning.pendingTrials.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No adaptive trials yet. Start one above to teach Arlo your preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloTextMuted
                        )
                    }
                }
            }
        } else {
            items(memory.learning.pendingTrials, key = { it.id }) { trial ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trial_card_${trial.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
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
                            Text(
                                text = trial.proposedChange,
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (trial.status) {
                                    "approved" -> ArloPrimaryContainer
                                    "rejected" -> ArloDangerContainer
                                    else -> ArloSecondaryContainer
                                }
                            ) {
                                Text(
                                    text = trial.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (trial.status) {
                                        "approved" -> ArloPrimary
                                        "rejected" -> ArloDanger
                                        else -> ArloSecondary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (trial.status == "trial") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onTrialFeedback(trial.id, true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ArloPrimary,
                                        contentColor = ArloOnPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("keep_trial_${trial.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Keep it")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { onTrialFeedback(trial.id, false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextSecondary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Undo")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Memory Records Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Memory entries (${memory.entries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArloTextPrimary
                )
                TextButton(onClick = { showAddEntryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ArloPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add record", color = ArloPrimary)
                }
            }
        }

        if (memory.entries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No explicit memory records stored. You can add one or let Arlo propose records.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloTextMuted
                        )
                    }
                }
            }
        } else {
            items(memory.entries, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.summary,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (entry.status == "revoked") ArloTextMuted else ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Source: ${entry.sourceLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextMuted
                                    )
                                    Text(
                                        text = "• ${entry.confidence}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloPrimary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteMemoryEntry(entry.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = ArloTextMuted)
                            }
                        }

                        if (entry.status != "revoked") {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { onRevokeSource(entry.sourceType) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = ArloDanger, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Revoke this source category", color = ArloDanger, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        AlertDialog(
            onDismissRequest = { showAddEntryDialog = false },
            title = { Text("Add Memory Record") },
            text = {
                Column {
                    Text(
                        "Explicit memory allows Arlo to assist with context you choose to preserve.",
                        color = ArloTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEntrySummary,
                        onValueChange = { newEntrySummary = it },
                        label = { Text("Memory summary") },
                        placeholder = { Text("e.g. Enjoys mornings for creative work") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEntrySourceLabel,
                        onValueChange = { newEntrySourceLabel = it },
                        label = { Text("Source label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEntrySummary.isNotBlank()) {
                            onAddMemoryEntry(newEntrySummary, "user_text", newEntrySourceLabel)
                            newEntrySummary = ""
                            showAddEntryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEntryDialog = false }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
