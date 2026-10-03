package com.example.arlo.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.R
import com.example.arlo.data.AmbientWeatherManager
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.CadenceReminderManager
import com.example.arlo.data.ConnectedAppsManager
import com.example.arlo.data.FloatingBubblePreferenceManager
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.GoalStateManager
import com.example.arlo.data.JournalStateManager
import com.example.arlo.data.NaturalRhythmManager
import com.example.arlo.data.ResourceIntelligenceManager
import com.example.arlo.model.ArloState
import com.example.arlo.ui.components.AmbientWeatherDialog
import com.example.arlo.ui.components.ArloBubbleButton
import com.example.arlo.ui.components.ArloBubbleDialog
import com.example.arlo.ui.components.CatWorkingDialog
import com.example.arlo.ui.components.CatchUpCheckpointDialog
import com.example.arlo.ui.components.ConnectedAppsDialog
import com.example.arlo.ui.components.ConnectorsDialog
import com.example.arlo.ui.components.DailyCheckInPromptDialog
import com.example.arlo.ui.components.FloatingBubbleSettingsDialog
import com.example.arlo.ui.components.GeminiSettingsDialog
import com.example.arlo.ui.components.NaturalRhythmDialog
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ArloTab(val label: String, val icon: String) {
    TODAY("Today", "☀"),
    GOALS("Moving Toward", "🎯"),
    RESOURCES("Resources", "💡"),
    JOURNAL("Journal", "✎"),
    LINKS("Links", "🔖"),
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
    var showGeminiSettingsDialog by remember { mutableStateOf(false) }

    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val hasCheckedInToday = state.lastCheckIn?.startsWith(todayDateStr) == true
    var showDailyCheckInPromptOnLaunch by remember { mutableStateOf(!hasCheckedInToday) }

    val context = LocalContext.current
    val geminiManager = remember { GeminiManager(context) }
    val ambientWeatherManager = remember { AmbientWeatherManager(context) }
    val connectedAppsManager = remember { ConnectedAppsManager(context) }
    val floatingBubbleManager = remember { FloatingBubblePreferenceManager(context) }
    val rhythmManager = remember { NaturalRhythmManager(context) }
    val cadenceManager = remember { CadenceReminderManager(context, rhythmManager) }
    val resourceManager = remember { ResourceIntelligenceManager(context) }

    var showAmbientWeatherDialog by remember { mutableStateOf(false) }
    var showConnectedAppsDialog by remember { mutableStateOf(false) }
    var showFloatingBubbleSettingsDialog by remember { mutableStateOf(false) }
    var showCatchUpDialog by remember { mutableStateOf(false) }
    var showNaturalRhythmDialog by remember { mutableStateOf(false) }

    val atmosphere by ambientWeatherManager.atmosphereFlow.collectAsState()
    val isFloatingBubbleActive by floatingBubbleManager.isBubbleEnabled.collectAsState()
    val gamification by cadenceManager.gamificationFlow.collectAsState()
    val isCheckpointDue by cadenceManager.showCheckpointDialog.collectAsState()

    LaunchedEffect(isCheckpointDue) {
        if (isCheckpointDue) {
            showCatchUpDialog = true
        }
    }

    val goalStateManager = remember(repository) { GoalStateManager(repository) }
    val journalStateManager = remember(repository) { JournalStateManager(repository) }

    val coroutineScope = rememberCoroutineScope()
    var isCatWorking by remember { mutableStateOf(false) }
    var isCatDone by remember { mutableStateOf(false) }
    var catActionTitle by remember { mutableStateOf("Arlo Cat is auditing your vault...") }
    var catDoneSaying by remember { mutableStateOf<String?>(null) }

    val connections by repository.connections.collectAsState()

    // Handle back button to return to Today tab
    BackHandler(enabled = activeTab != ArloTab.TODAY) {
        activeTab = ArloTab.TODAY
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArloPrimaryContainer)
                                    .clickable {
                                        catActionTitle = "Auditing Vault & Running Diagnostics..."
                                        catDoneSaying = "Purr-fect! Everything is encrypted, healthy, and cozy."
                                        isCatWorking = true
                                        isCatDone = false
                                        coroutineScope.launch {
                                            delay(2400)
                                            repository.audit("cat routine check performed")
                                            isCatWorking = false
                                            isCatDone = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cat_outline),
                                    contentDescription = "Arlo Cat Logo",
                                    tint = ArloPrimary,
                                    modifier = Modifier.size(24.dp)
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
                        // Weather & Time Badge Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .clickable { showAmbientWeatherDialog = true }
                                .padding(end = 4.dp)
                                .testTag("weather_top_action")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${atmosphere.timeOfDay.icon} ${atmosphere.weather.icon}", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${atmosphere.temperatureF}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = atmosphere.timeOfDay.themeAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        val geminiConfig by geminiManager.configFlow.collectAsState()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloPrimaryContainer,
                            modifier = Modifier
                                .clickable { showGeminiSettingsDialog = true }
                                .padding(end = 4.dp)
                                .testTag("gemini_settings_top_action")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(geminiConfig.activeTier.badgeIcon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Gemini",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Natural Rhythm & Heartbeat Chip Action
                        val rhythmProfile by rhythmManager.rhythmProfileFlow.collectAsState()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .clickable { showNaturalRhythmDialog = true }
                                .padding(end = 4.dp)
                                .testTag("natural_rhythm_top_action")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rhythmProfile.chronotype.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${rhythmProfile.adaptiveHeartbeatHours}h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // 32-Hour Catch-Up Checkpoint Action
                        IconButton(
                            onClick = { showCatchUpDialog = true },
                            modifier = Modifier.testTag("catchup_checkpoint_top_action")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (isCheckpointDue) {
                                        Badge(containerColor = ArloWarmGold) {
                                            Text("!", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Text("⏱️", fontSize = 16.sp)
                            }
                        }

                        // Gamification status badge if enabled
                        if (gamification.isEnabled) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ArloPrimaryContainer,
                                modifier = Modifier
                                    .clickable { showCatchUpDialog = true }
                                    .padding(end = 4.dp)
                                    .testTag("gamification_status_top_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎮 Lvl ${gamification.level}", style = MaterialTheme.typography.labelSmall, color = ArloPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("🐟${gamification.tunaTreats}", style = MaterialTheme.typography.labelSmall, color = ArloWarmGold, fontSize = 10.sp)
                                }
                            }
                        }

                        // Floating Bubble / Chat Head Action
                        IconButton(
                            onClick = { showFloatingBubbleSettingsDialog = true },
                            modifier = Modifier.testTag("floating_bubble_top_action")
                        ) {
                            Text(if (isFloatingBubbleActive) "💬" else "💭", fontSize = 16.sp)
                        }

                        // Connected Apps Hub Action
                        IconButton(
                            onClick = { showConnectedAppsDialog = true },
                            modifier = Modifier.testTag("connected_apps_top_action")
                        ) {
                            Text("🔗", fontSize = 16.sp)
                        }

                        IconButton(
                            onClick = {
                                catActionTitle = "Baking Biscuits & Organizing Data..."
                                catDoneSaying = "Meow-gical! All tasks and thoughts are neatly organized."
                                isCatWorking = true
                                isCatDone = false
                                coroutineScope.launch {
                                    delay(2200)
                                    isCatWorking = false
                                    isCatDone = true
                                }
                            },
                            modifier = Modifier.testTag("cat_work_summon_button")
                        ) {
                            Text("🐾", fontSize = 18.sp)
                        }
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
                if (!isTablet) {
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
                                    val iconText = if (tab == ArloTab.TODAY) {
                                        atmosphere.todayModuleIcon
                                    } else {
                                        tab.icon
                                    }
                                    Text(
                                        text = iconText,
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
                }
            },
            containerColor = ArloDarkBackground
        ) { paddingValues ->
            val screenContent: @Composable (Modifier) -> Unit = { innerModifier ->
                Box(modifier = innerModifier) {
                    when (activeTab) {
                        ArloTab.TODAY -> TodayScreen(
                            state = state,
                            geminiManager = geminiManager,
                            onAddTask = {
                                repository.addTask(it)
                                val msg = cadenceManager.awardProgress(15, 1)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onToggleTask = {
                                repository.toggleTask(it)
                                val msg = cadenceManager.awardProgress(25, 1)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onDeleteTask = { repository.deleteTask(it) },
                            onCheckIn = { moodIndex, reflectionText, energyLabel ->
                                repository.checkIn(moodIndex)
                                if (reflectionText.isNotBlank()) {
                                    repository.addNote(
                                        body = reflectionText,
                                        mood = energyLabel,
                                        prompt = "Daily Check-in: $energyLabel energy"
                                    )
                                }
                                val msg = cadenceManager.awardProgress(30, 2)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                catActionTitle = "Calibrating Daily Rhythm & Patterns..."
                                catDoneSaying = "Purr-fect! Check-in recorded and pattern analysis updated."
                                isCatWorking = true
                                isCatDone = false
                                coroutineScope.launch {
                                    delay(1200)
                                    isCatWorking = false
                                    isCatDone = true
                                }
                            },
                            onAdvanceGoal = { goal ->
                                catActionTitle = "Advancing Progress..."
                                catDoneSaying = "Purr-fect! Step progress incremented."
                                isCatWorking = true
                                isCatDone = false
                                coroutineScope.launch {
                                    delay(1400)
                                    goalStateManager.incrementProgress(goal.id, 1)
                                    val msg = cadenceManager.awardProgress(20, 1)
                                    if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    isCatWorking = false
                                    isCatDone = true
                                }
                            },
                            onCompleteMilestone = { goalId, milestoneId ->
                                catActionTitle = "Completing Step..."
                                catDoneSaying = "Meow-gical! Step completed and locked in."
                                isCatWorking = true
                                isCatDone = false
                                coroutineScope.launch {
                                    delay(1400)
                                    goalStateManager.toggleMilestone(goalId, milestoneId)
                                    val msg = cadenceManager.awardProgress(40, 2)
                                    if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    isCatWorking = false
                                    isCatDone = true
                                }
                            },
                            ambientWeatherManager = ambientWeatherManager,
                            onOpenGeminiSettings = { showGeminiSettingsDialog = true },
                            onOpenWeatherSettings = { showAmbientWeatherDialog = true },
                            onOpenConnectedApps = { showConnectedAppsDialog = true },
                            onOpenConnectors = { showConnectorsDialog = true }
                        )
                        ArloTab.GOALS -> GoalsScreen(
                            goalStateManager = goalStateManager
                        )
                        ArloTab.RESOURCES -> ResourcesScreen(
                            state = state,
                            resourceManager = resourceManager,
                            geminiManager = geminiManager,
                            onSaveToVault = { url, title, notes, tags ->
                                repository.addSavedLink(
                                    url = url,
                                    title = title,
                                    notes = notes,
                                    tags = tags,
                                    isFavorite = true
                                )
                                val msg = cadenceManager.awardProgress(20, 1)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                        ArloTab.JOURNAL -> JournalScreen(
                            journalStateManager = journalStateManager
                        )
                        ArloTab.LINKS -> LinksScreen(
                            savedLinks = state.savedLinks,
                            onAddLink = { url, title, notes, tags, isFavorite ->
                                repository.addSavedLink(url, title, notes, tags, isFavorite)
                                val msg = cadenceManager.awardProgress(20, 1)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onUpdateLink = { link ->
                                repository.updateSavedLink(link)
                            },
                            onDeleteLink = { id ->
                                repository.deleteSavedLink(id)
                            },
                            onToggleFavorite = { id ->
                                repository.toggleFavoriteLink(id)
                            },
                            onToggleRead = { id ->
                                repository.toggleReadLink(id)
                                rhythmManager.recordInteraction("toggle_link")
                                val msg = cadenceManager.awardProgress(15, 1)
                                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
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

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavigationRail(
                        containerColor = ArloDarkSurface,
                        modifier = Modifier.fillMaxHeight().testTag("tablet_nav_rail"),
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ArloPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_cat_outline),
                                        contentDescription = "Arlo",
                                        tint = ArloPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Arlo", fontWeight = FontWeight.Bold, color = ArloTextPrimary, fontSize = 12.sp)
                            }
                        }
                    ) {
                        ArloTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { activeTab = tab },
                                icon = {
                                    val iconText = if (tab == ArloTab.TODAY) atmosphere.todayModuleIcon else tab.icon
                                    Text(iconText, fontSize = 20.sp)
                                },
                                label = {
                                    Text(
                                        tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ArloPrimary,
                                    selectedTextColor = ArloPrimary,
                                    unselectedIconColor = ArloTextMuted,
                                    unselectedTextColor = ArloTextMuted,
                                    indicatorColor = ArloPrimaryContainer
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        screenContent(
                            Modifier
                                .fillMaxSize()
                                .widthIn(max = 1100.dp)
                        )
                    }
                }
            } else {
                screenContent(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
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
            repository = repository,
            connections = connections,
            onRequestConnection = { provider ->
                repository.requestConnection(provider)
            },
            onDismiss = { showConnectorsDialog = false }
        )
    }

    if (showGeminiSettingsDialog) {
        GeminiSettingsDialog(
            geminiManager = geminiManager,
            onDismiss = { showGeminiSettingsDialog = false }
        )
    }

    if (showDailyCheckInPromptOnLaunch) {
        DailyCheckInPromptDialog(
            onCompleteCheckIn = { moodIndex, reflectionText, energyLabel ->
                repository.checkIn(moodIndex)
                if (reflectionText.isNotBlank()) {
                    repository.addNote(
                        body = reflectionText,
                        mood = energyLabel,
                        prompt = "Daily Check-in: $energyLabel energy"
                    )
                }
                showDailyCheckInPromptOnLaunch = false
                catActionTitle = "Calibrating Daily Rhythm & Patterns..."
                catDoneSaying = "Purr-fect! Morning check-in recorded."
                isCatWorking = true
                isCatDone = false
                coroutineScope.launch {
                    delay(1200)
                    isCatWorking = false
                    isCatDone = true
                }
            },
            onDismiss = { showDailyCheckInPromptOnLaunch = false }
        )
    }

    if (showAmbientWeatherDialog) {
        AmbientWeatherDialog(
            ambientWeatherManager = ambientWeatherManager,
            onDismiss = { showAmbientWeatherDialog = false }
        )
    }

    if (showConnectedAppsDialog) {
        ConnectedAppsDialog(
            connectedAppsManager = connectedAppsManager,
            onDismiss = { showConnectedAppsDialog = false }
        )
    }

    if (showFloatingBubbleSettingsDialog) {
        FloatingBubbleSettingsDialog(
            floatingBubbleManager = floatingBubbleManager,
            onDismiss = { showFloatingBubbleSettingsDialog = false }
        )
    }

    if (showCatchUpDialog) {
        CatchUpCheckpointDialog(
            briefing = cadenceManager.generateBriefing(state),
            cadenceManager = cadenceManager,
            onDismiss = { showCatchUpDialog = false }
        )
    }

    if (showNaturalRhythmDialog) {
        NaturalRhythmDialog(
            rhythmManager = rhythmManager,
            onDismiss = { showNaturalRhythmDialog = false }
        )
    }

    if (isCatWorking || isCatDone) {
        CatWorkingDialog(
            isWorking = isCatWorking,
            actionTitle = catActionTitle,
            isDone = isCatDone,
            customDoneSaying = catDoneSaying,
            onDismiss = {
                isCatWorking = false
                isCatDone = false
            }
        )
    }
}
