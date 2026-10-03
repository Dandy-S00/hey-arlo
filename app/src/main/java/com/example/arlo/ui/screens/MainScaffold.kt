package com.example.arlo.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.*
import com.example.arlo.model.ArloState
import com.example.arlo.ui.components.*
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MainSection(val label: String, val icon: String, val testTag: String) {
    COMPANION("Talk with Arlo", "🐾", "nav_section_companion"),
    LIFE_VAULT("Life Vault & Data", "🗄️", "nav_section_vault")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    state: ArloState,
    repository: ArloRepository,
    onLockArlo: () -> Unit,
    onResetVault: () -> Unit
) {
    var activeSection by remember { mutableStateOf(MainSection.COMPANION) }
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
    val smsAndEmailManager = remember { SmsAndEmailManager(context) }
    val catNotificationManager = remember { com.example.arlo.notification.CatNotificationManager(context) }

    var showAmbientWeatherDialog by remember { mutableStateOf(false) }
    var showConnectedAppsDialog by remember { mutableStateOf(false) }
    var showFloatingBubbleSettingsDialog by remember { mutableStateOf(false) }
    var showCatchUpDialog by remember { mutableStateOf(false) }
    var showNaturalRhythmDialog by remember { mutableStateOf(false) }
    var showLiveVoiceDialog by remember { mutableStateOf(false) }
    var showAudioTranscriptionDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var showSmsAndEmailReaderDialog by remember { mutableStateOf(false) }
    var showAudioAndCallRecorderDialog by remember { mutableStateOf(false) }
    var showVideoToTextDialog by remember { mutableStateOf(false) }
    var showCatReminderSchedulerDialog by remember { mutableStateOf(false) }

    val roomTasks by repository.roomTasks.collectAsState(initial = emptyList())
    val roomGoals by repository.roomGoals.collectAsState(initial = emptyList())
    val roomReflections by repository.roomReflections.collectAsState(initial = emptyList())
    val smsMessages by smsAndEmailManager.smsListFlow.collectAsState()
    val emails by smsAndEmailManager.emailListFlow.collectAsState()

    val atmosphere by ambientWeatherManager.atmosphereFlow.collectAsState()
    val isFloatingBubbleActive by floatingBubbleManager.isBubbleEnabled.collectAsState()
    val gamification by cadenceManager.gamificationFlow.collectAsState()
    val isCheckpointDue by cadenceManager.showCheckpointDialog.collectAsState()

    LaunchedEffect(isCheckpointDue) {
        if (isCheckpointDue) {
            showCatchUpDialog = true
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var isCatWorking by remember { mutableStateOf(false) }
    var isCatDone by remember { mutableStateOf(false) }
    var catActionTitle by remember { mutableStateOf("Arlo is synchronizing vault data...") }
    var catDoneSaying by remember { mutableStateOf<String?>(null) }

    val connections by repository.connections.collectAsState()

    // Handle back button to return to Companion section
    BackHandler(enabled = activeSection != MainSection.COMPANION) {
        activeSection = MainSection.COMPANION
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("app_top_bar_title")
                        ) {
                            Text(
                                text = "Arlo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = ArloTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ArloPrimaryContainer,
                                modifier = Modifier.clickable { showAmbientWeatherDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(atmosphere.weather.icon, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = atmosphere.weather.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Global Search Button
                        IconButton(
                            onClick = { showGlobalSearchDialog = true },
                            modifier = Modifier.testTag("open_global_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = ArloPrimary)
                        }

                        // Cat Reminders & Purr Notifications
                        IconButton(
                            onClick = { showCatReminderSchedulerDialog = true },
                            modifier = Modifier.testTag("cat_reminders_top_action")
                        ) {
                            Text("🔔", fontSize = 16.sp)
                        }

                        // Record Live Audio / Call Action
                        IconButton(
                            onClick = { showAudioAndCallRecorderDialog = true },
                            modifier = Modifier.testTag("record_audio_call_top_action")
                        ) {
                            Text("🎙️", fontSize = 16.sp)
                        }

                        // Video Link to Text Action
                        IconButton(
                            onClick = { showVideoToTextDialog = true },
                            modifier = Modifier.testTag("video_to_text_top_action")
                        ) {
                            Text("📹", fontSize = 16.sp)
                        }

                        // Connected Apps Hub Action
                        IconButton(
                            onClick = { showConnectedAppsDialog = true },
                            modifier = Modifier.testTag("connected_apps_top_action")
                        ) {
                            Text("🔗", fontSize = 16.sp)
                        }

                        // Connectors & All-Integrations Sync
                        IconButton(
                            onClick = { showConnectorsDialog = true },
                            modifier = Modifier.testTag("open_connectors_top_action")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync Integrations", tint = ArloWarmGold)
                        }

                        // Gemini Model Settings
                        IconButton(
                            onClick = { showGeminiSettingsDialog = true },
                            modifier = Modifier.testTag("gemini_settings_top_action")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Settings", tint = ArloTextSecondary)
                        }

                        // Emergency Passphrase Lock
                        IconButton(
                            onClick = onLockArlo,
                            modifier = Modifier.testTag("lock_arlo_top_action")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = ArloTextMuted)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ArloDarkSurface,
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
                        MainSection.values().forEach { section ->
                            val isSelected = activeSection == section
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { activeSection = section },
                                label = {
                                    Text(
                                        text = section.label,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                icon = {
                                    Text(
                                        text = section.icon,
                                        fontSize = if (isSelected) 24.sp else 20.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ArloPrimary,
                                    selectedTextColor = ArloPrimary,
                                    unselectedIconColor = ArloTextMuted,
                                    unselectedTextColor = ArloTextMuted,
                                    indicatorColor = ArloPrimaryContainer
                                ),
                                modifier = Modifier.testTag(section.testTag)
                            )
                        }
                    }
                }
            },
            containerColor = ArloDarkBackground
        ) { paddingValues ->
            val screenContent: @Composable (Modifier) -> Unit = { innerModifier ->
                Box(modifier = innerModifier) {
                    when (activeSection) {
                        MainSection.COMPANION -> {
                            ArloCompanionScreen(
                                state = state,
                                repository = repository,
                                geminiManager = geminiManager,
                                onOpenLiveVoice = { showLiveVoiceDialog = true },
                                onOpenAudioTranscribeDialog = { showAudioTranscriptionDialog = true },
                                onOpenConnectors = { showConnectorsDialog = true },
                                onOpenAudioRecorder = { showAudioAndCallRecorderDialog = true },
                                onOpenVideoToText = { showVideoToTextDialog = true },
                                onOpenCatReminders = { showCatReminderSchedulerDialog = true }
                            )
                        }
                        MainSection.LIFE_VAULT -> {
                            LifeVaultScreen(
                                state = state,
                                repository = repository,
                                onLockArlo = onLockArlo,
                                onResetVault = onResetVault,
                                onOpenConnectors = { showConnectorsDialog = true },
                                onOpenCloudSync = { showCloudSyncDialog = true },
                                onOpenGlobalSearch = { showGlobalSearchDialog = true }
                            )
                        }
                    }
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
                        modifier = Modifier.width(180.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        MainSection.values().forEach { section ->
                            val isSelected = activeSection == section
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { activeSection = section },
                                label = { Text(section.label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                icon = { Text(section.icon, fontSize = 22.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ArloPrimary,
                                    selectedTextColor = ArloPrimary,
                                    unselectedIconColor = ArloTextMuted,
                                    unselectedTextColor = ArloTextMuted,
                                    indicatorColor = ArloPrimaryContainer
                                ),
                                modifier = Modifier.testTag(section.testTag)
                            )
                        }
                    }
                    screenContent(Modifier.fillMaxSize())
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

    // Floating Bubble Overlay
    if (isFloatingBubbleActive) {
        ArloBubbleButton(
            avatarSymbol = state.avatar,
            onClick = { activeSection = MainSection.COMPANION }
        )
    }

    // Dialogs
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

    if (showLiveVoiceDialog) {
        LiveVoiceConversationDialog(
            geminiManager = geminiManager,
            state = state,
            onDismiss = { showLiveVoiceDialog = false }
        )
    }

    if (showAudioTranscriptionDialog) {
        AudioTranscriptionDialog(
            geminiManager = geminiManager,
            onDismiss = { showAudioTranscriptionDialog = false },
            onTranscribedText = { text, destination ->
                when (destination) {
                    "task" -> {
                        repository.addTask(text)
                        Toast.makeText(context, "Added sprint task from voice!", Toast.LENGTH_SHORT).show()
                    }
                    "goal" -> {
                        repository.addGoal(title = text, detail = "Transcribed via voice note")
                        Toast.makeText(context, "Added intention goal from voice!", Toast.LENGTH_SHORT).show()
                    }
                    "journal" -> {
                        repository.addNote(body = text, mood = "Steady", prompt = "Audio Voice Note")
                        Toast.makeText(context, "Recorded reflection from voice!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showCloudSyncDialog) {
        CloudSyncDialog(
            repository = repository,
            geminiManager = geminiManager,
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    if (showGlobalSearchDialog) {
        GlobalRoomSearchDialog(
            roomTasks = roomTasks,
            roomGoals = roomGoals,
            roomReflections = roomReflections,
            savedLinks = state.savedLinks,
            smsMessages = smsMessages,
            emails = emails,
            onToggleTask = { repository.toggleTask(it) },
            onConvertExternalToTask = {
                repository.addTask(it)
                Toast.makeText(context, "Added as sprint task!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showGlobalSearchDialog = false }
        )
    }

    if (showSmsAndEmailReaderDialog) {
        SmsAndEmailReaderDialog(
            smsAndEmailManager = smsAndEmailManager,
            onConvertToTask = {
                repository.addTask(it)
                Toast.makeText(context, "Added to tasks!", Toast.LENGTH_SHORT).show()
            },
            onConvertToGoal = {
                repository.addGoal(title = it, detail = "Imported from communication")
                Toast.makeText(context, "Added to goals!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSmsAndEmailReaderDialog = false }
        )
    }

    if (showConnectorsDialog) {
        ConnectorsDialog(
            repository = repository,
            connections = connections,
            onRequestConnection = { repository.requestConnection(it) },
            onDismiss = { showConnectorsDialog = false }
        )
    }

    if (showAudioAndCallRecorderDialog) {
        AudioAndCallRecorderDialog(
            geminiManager = geminiManager,
            onDismiss = { showAudioAndCallRecorderDialog = false },
            onSaveNoteToVault = { title, body, mood ->
                repository.addNote(body = body, mood = mood, prompt = title)
            },
            onAddActionItemsToTasks = { items ->
                items.forEach { taskTitle -> repository.addTask(taskTitle) }
            }
        )
    }

    if (showVideoToTextDialog) {
        VideoToTextDialog(
            geminiManager = geminiManager,
            onDismiss = { showVideoToTextDialog = false },
            onSaveNoteToVault = { title, body, mood ->
                repository.addNote(body = body, mood = mood, prompt = title)
            },
            onSaveLinkToResources = { url, title, notes, tags ->
                repository.addSavedLink(url = url, title = title, notes = notes, tags = tags, isFavorite = true)
            },
            onAddActionItemsToTasks = { items ->
                items.forEach { taskTitle -> repository.addTask(taskTitle) }
            }
        )
    }

    if (showCatReminderSchedulerDialog) {
        com.example.arlo.ui.components.CatReminderSchedulerDialog(
            tasks = state.tasks,
            notificationManager = catNotificationManager,
            onDismiss = { showCatReminderSchedulerDialog = false }
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
