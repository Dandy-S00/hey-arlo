package com.example.arlo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.*
import com.example.arlo.model.ArloState
import com.example.arlo.ui.components.CatBackgroundPaws
import com.example.arlo.ui.components.InteractivePetArloWidget
import com.example.arlo.ui.theme.*

enum class VaultSection(val label: String, val icon: String) {
    TODAY_RHYTHM("Today & Rhythm", "☀"),
    TASKS_GOALS("Tasks & Goals", "🎯"),
    NOTES_JOURNAL("Notes & Journal", "✎"),
    MEMORIES_HEARTBEAT("Memories & Heartbeat", "🧠"),
    RESOURCES_LINKS("Resources & Links", "🔖"),
    INTEGRATIONS_SYNC("Integrations & Sync", "🔄"),
    PRIVACY_VAULT("Privacy & Security", "🔒"),
    SETTINGS("Settings & Profile", "⚙️")
}

@Composable
fun LifeVaultScreen(
    state: ArloState,
    repository: ArloRepository,
    onLockArlo: () -> Unit,
    onResetVault: () -> Unit,
    onOpenConnectors: () -> Unit,
    onOpenCloudSync: () -> Unit,
    onOpenGlobalSearch: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(VaultSection.TODAY_RHYTHM) }

    val geminiManager = remember { GeminiManager(context) }
    val ambientWeatherManager = remember { AmbientWeatherManager(context) }
    val goalStateManager = remember(repository) { GoalStateManager(repository) }
    val journalStateManager = remember(repository) { JournalStateManager(repository) }
    val rhythmManager = remember { NaturalRhythmManager(context) }
    val cadenceManager = remember { CadenceReminderManager(context, rhythmManager) }
    val gamification by cadenceManager.gamificationFlow.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(ArloDarkBackground)) {
        // Ambient background paw prints
        CatBackgroundPaws()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Vault Header with Quick Search & Cloud Status
            Surface(
                color = ArloDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ArloPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(state.avatar.ifBlank { "🐱" }, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Life Vault & Data Hub",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🐾", fontSize = 12.sp)
                                }
                                Text(
                                    text = "All notes, heartbeat, memories, tasks & resources",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onOpenGlobalSearch,
                                modifier = Modifier.size(34.dp).testTag("vault_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = ArloPrimary, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = onOpenConnectors,
                                modifier = Modifier.size(34.dp).testTag("vault_sync_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", tint = ArloWarmGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vault Sub-Section Navigation Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(VaultSection.values()) { section ->
                            val isSelected = selectedSection == section
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArloPrimary else ArloBorder
                                ),
                                modifier = Modifier
                                    .clickable { selectedSection = section }
                                    .testTag("vault_tab_${section.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(section.icon, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = section.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedSection) {
                    VaultSection.TODAY_RHYTHM -> {
                        TodayScreen(
                            state = state,
                            geminiManager = geminiManager,
                            ambientWeatherManager = ambientWeatherManager,
                            onAddTask = { repository.addTask(it) },
                            onToggleTask = { repository.toggleTask(it) },
                            onDeleteTask = { repository.deleteTask(it) },
                            onCheckIn = { mood, text, energy ->
                                repository.checkIn(mood)
                                if (text.isNotBlank()) {
                                    repository.addNote(body = text, mood = energy, prompt = "Daily Check-in: $energy energy")
                                }
                            },
                            onOpenConnectors = onOpenConnectors
                        )
                    }
                    VaultSection.TASKS_GOALS -> {
                        GoalsScreen(
                            goalStateManager = goalStateManager,
                            repository = repository
                        )
                    }
                    VaultSection.NOTES_JOURNAL -> {
                        JournalScreen(
                            journalStateManager = journalStateManager
                        )
                    }
                    VaultSection.MEMORIES_HEARTBEAT -> {
                        MemoriesAndHeartbeatView(
                            state = state,
                            repository = repository,
                            tunaTreats = gamification.tunaTreats,
                            onPetArlo = {
                                cadenceManager.awardProgress(10, 1)
                                Toast.makeText(context, "Prrrrr... Arlo purred! +1 Tuna Treat 🐟", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    VaultSection.RESOURCES_LINKS -> {
                        LinksScreen(
                            savedLinks = state.savedLinks,
                            onAddLink = { url, title, notes, tags, fav ->
                                repository.addSavedLink(url, title, notes, tags, fav)
                            },
                            onUpdateLink = { repository.updateSavedLink(it) },
                            onDeleteLink = { repository.deleteSavedLink(it) },
                            onToggleFavorite = { repository.toggleFavoriteLink(it) },
                            onToggleRead = { repository.toggleReadLink(it) }
                        )
                    }
                    VaultSection.INTEGRATIONS_SYNC -> {
                        IntegrationsVaultView(
                            repository = repository,
                            onOpenConnectors = onOpenConnectors,
                            onOpenCloudSync = onOpenCloudSync
                        )
                    }
                    VaultSection.PRIVACY_VAULT -> {
                        PrivacyScreen(
                            permissions = state.permissions,
                            auditTrail = state.audit,
                            onUpdatePermission = { key, enabled -> repository.updatePermission(key, enabled) },
                            onPauseAll = { repository.pauseAllPermissions() },
                            onLockArlo = onLockArlo,
                            onChangePassphrase = { old, new -> repository.changePassphrase(old, new) },
                            onExportBackup = { repository.exportDecryptedJson() },
                            onResetVault = onResetVault
                        )
                    }
                    VaultSection.SETTINGS -> {
                        UserSettingsScreen(
                            state = state,
                            repository = repository,
                            geminiManager = geminiManager,
                            onLockArlo = onLockArlo,
                            onResetVault = onResetVault,
                            onOpenConnectors = onOpenConnectors,
                            onOpenCloudSync = onOpenCloudSync
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoriesAndHeartbeatView(
    state: ArloState,
    repository: ArloRepository,
    tunaTreats: Int,
    onPetArlo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Pet Arlo widget
        InteractivePetArloWidget(
            tunaTreats = tunaTreats,
            onPet = onPetArlo
        )

        // Heartbeat Status Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ArloDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💓", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Arlo Heartbeat & Rhythm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "Local pulse active • Calibrated to user rhythm",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloSuccess,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArloPrimaryContainer
                    ) {
                        Text(
                            text = "HEALTHY",
                            color = ArloPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Tasks", color = ArloTextMuted, fontSize = 10.sp)
                        Text("${state.tasks.count { !it.done }} sprint items", color = ArloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Goals Tracked", color = ArloTextMuted, fontSize = 10.sp)
                        Text("${state.goals.size} intentions", color = ArloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Reflections", color = ArloTextMuted, fontSize = 10.sp)
                        Text("${state.notes.size} journal entries", color = ArloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Agent Core Memory & Learned Habits
        Text(
            text = "Agent Memory & Learned Context",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArloTextPrimary
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ArloDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Learned Rhythm Patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArloPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Peak Focus: Morning sprints\n• Check-in cadence: Daily morning reflection\n• Mood baseline: Reflective & Steady\n• Arlo Biscuit Status: Fresh & Purring 🐾",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArloTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Privacy Ledger Audit summary
        Text(
            text = "Recent Privacy & Security Ledger",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ArloTextPrimary
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ArloDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.audit.takeLast(15).reversed()) { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🐾 ${entry.action}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = entry.at.take(16),
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntegrationsVaultView(
    repository: ArloRepository,
    onOpenConnectors: () -> Unit,
    onOpenCloudSync: () -> Unit
) {
    val connections by repository.connections.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ArloDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Connected Integrations Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArloTextPrimary
                        )
                        Text(
                            text = "${connections.size} active services connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onOpenConnectors,
                        colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Manage & Sync All", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ArloDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Firebase Cloud Vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArloTextPrimary
                )
                Text(
                    text = "End-to-end encrypted backup envelope synced with Firestore",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArloTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenCloudSync,
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimaryContainer, contentColor = ArloPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cloud Sync Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
