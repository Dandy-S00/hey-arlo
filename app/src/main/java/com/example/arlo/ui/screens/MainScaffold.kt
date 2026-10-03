package com.example.arlo.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.ArloRepository
import com.example.arlo.model.ArloState
import com.example.arlo.ui.components.ArloBubbleButton
import com.example.arlo.ui.components.ArloBubbleDialog
import com.example.arlo.ui.components.ConnectorsDialog
import com.example.arlo.ui.theme.*

enum class ArloTab(val label: String, val icon: String) {
    TODAY("Today", "☀"),
    GOALS("Goals", "◈"),
    JOURNAL("Journal", "✎"),
    MEMORY("Memory", "✦"),
    PRIVACY("Privacy", "⌁")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    state: ArloState,
    repository: ArloRepository,
    onLockArlo: () -> Unit,
    onResetVault: () -> Unit
) {
    var activeTab by remember { mutableStateOf(ArloTab.TODAY) }
    var showArloBubbleDialog by remember { mutableStateOf(false) }
    var showConnectorsDialog by remember { mutableStateOf(false) }

    val connections by repository.connections.collectAsState()

    // Handle back button to return to Today tab
    BackHandler(enabled = activeTab != ArloTab.TODAY) {
        activeTab = ArloTab.TODAY
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(ArloPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.avatar,
                                fontSize = 16.sp,
                                color = ArloPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Arlo",
                            fontWeight = FontWeight.Bold,
                            color = ArloTextPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloDarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ArloSuccess, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Local vault",
                                    fontSize = 11.sp,
                                    color = ArloTextSecondary
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConnectorsDialog = true },
                        modifier = Modifier.testTag("open_connectors_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cable,
                            contentDescription = "Connect APIs",
                            tint = ArloPrimary
                        )
                    }
                    IconButton(
                        onClick = onLockArlo,
                        modifier = Modifier.testTag("lock_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Arlo",
                            tint = ArloTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArloDarkBackground,
                    titleContentColor = ArloTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ArloDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                ArloTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = {
                            Text(
                                text = tab.icon,
                                fontSize = if (isSelected) 20.sp else 17.sp,
                                color = if (isSelected) ArloPrimary else ArloTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                color = if (isSelected) ArloPrimary else ArloTextMuted,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArloPrimary,
                            unselectedIconColor = ArloTextMuted,
                            selectedTextColor = ArloPrimary,
                            unselectedTextColor = ArloTextMuted,
                            indicatorColor = ArloPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = ArloDarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                ArloTab.TODAY -> TodayScreen(
                    state = state,
                    onAddTask = { repository.addTask(it) },
                    onToggleTask = { repository.toggleTask(it) },
                    onDeleteTask = { repository.deleteTask(it) },
                    onCheckIn = { repository.checkIn(it) },
                    onOpenConnectors = { showConnectorsDialog = true }
                )
                ArloTab.GOALS -> GoalsScreen(
                    goals = state.goals,
                    onAddGoal = { title, detail -> repository.addGoal(title, detail) },
                    onToggleGoal = { repository.toggleGoal(it) },
                    onDeleteGoal = { repository.deleteGoal(it) }
                )
                ArloTab.JOURNAL -> JournalScreen(
                    notes = state.notes,
                    onAddNote = { repository.addNote(it) },
                    onDeleteNote = { repository.deleteNote(it) }
                )
                ArloTab.MEMORY -> MemoryScreen(
                    memory = state.memory,
                    onStartTrial = { repository.startAdaptiveTrial(it) },
                    onTrialFeedback = { id, keep -> repository.recordTrialFeedback(id, keep) },
                    onAddMemoryEntry = { summary, type, label -> repository.addMemoryEntry(summary, type, label) },
                    onRevokeSource = { repository.revokeSource(it) },
                    onDeleteMemoryEntry = { repository.deleteMemoryEntry(it) }
                )
                ArloTab.PRIVACY -> PrivacyScreen(
                    permissions = state.permissions,
                    auditTrail = state.audit,
                    onUpdatePermission = { key, enabled -> repository.updatePermission(key, enabled) },
                    onPauseAll = { repository.pauseAllPermissions() },
                    onLockArlo = onLockArlo,
                    onChangePassphrase = { oldPass, newPass -> repository.changePassphrase(oldPass, newPass) },
                    onExportBackup = { repository.exportEncryptedBackup() },
                    onResetVault = onResetVault
                )
            }

            // Floating Arlo companion bubble
            ArloBubbleButton(
                avatarSymbol = state.avatar,
                onClick = { showArloBubbleDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }

    if (showArloBubbleDialog) {
        ArloBubbleDialog(
            currentAvatar = state.avatar,
            onAvatarChange = {
                repository.setAvatar(it)
                showArloBubbleDialog = false
            },
            onDismiss = { showArloBubbleDialog = false }
        )
    }

    if (showConnectorsDialog) {
        ConnectorsDialog(
            connections = connections,
            onRequestConnection = { provider ->
                repository.requestConnection(provider)
            },
            onDismiss = { showConnectorsDialog = false }
        )
    }
}
