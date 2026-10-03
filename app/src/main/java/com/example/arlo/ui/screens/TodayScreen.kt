package com.example.arlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.AmbientAtmosphere
import com.example.arlo.data.AmbientWeatherManager
import com.example.arlo.data.ArloPatternAnalyzer
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.NaturalRhythmManager
import com.example.arlo.model.ArloState
import com.example.arlo.model.Goal
import com.example.arlo.ui.components.ArloDialogueBox
import com.example.arlo.ui.components.DailyCheckInPromptDialog
import com.example.arlo.ui.components.PeakProductivityDashboard
import com.example.arlo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    state: ArloState,
    geminiManager: GeminiManager,
    ambientWeatherManager: AmbientWeatherManager,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onCheckIn: (moodIndex: Int, reflectionText: String, energyLabel: String) -> Unit,
    onAdvanceGoal: (Goal) -> Unit = {},
    onCompleteMilestone: (goalId: String, milestoneId: String) -> Unit = { _, _ -> },
    onOpenGeminiSettings: () -> Unit = {},
    onOpenWeatherSettings: () -> Unit = {},
    onOpenConnectedApps: () -> Unit = {},
    onOpenRhythmDetails: () -> Unit = {},
    onOpenConnectors: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var newTaskTitle by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showCheckInDialog by remember { mutableStateOf(false) }

    val rhythmManager = remember { NaturalRhythmManager(context) }
    val rhythmProfile by rhythmManager.rhythmProfileFlow.collectAsState()

    val breakdown = remember(state) { ArloPatternAnalyzer.analyze(state) }
    val atmosphere by ambientWeatherManager.atmosphereFlow.collectAsState()

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val dateString = remember { dateFormat.format(Date()) }

    val completedTasks = state.tasks.count { it.done }
    val totalTasks = state.tasks.size

    val moods = listOf(
        Pair("😵", "Exhausted"),
        Pair("😕", "Low"),
        Pair("😐", "Steady"),
        Pair("🙂", "Good"),
        Pair("✨", "Inspired")
    )

    // Reusable Composable blocks for both Phone and Tablet layouts
    val headerBlock = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenWeatherSettings() }
                ) {
                    Text(
                        text = "${atmosphere.timeOfDay.icon} ${atmosphere.weather.icon} ${dateString.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArloDarkSurfaceVariant
                    ) {
                        Text(
                            text = "${atmosphere.temperatureF}°F",
                            style = MaterialTheme.typography.labelSmall,
                            color = atmosphere.timeOfDay.themeAccent,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "$greeting, you.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArloTextPrimary
                )
                Text(
                    text = atmosphere.headerGreetingSubtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloTextSecondary,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = { showCheckInDialog = true },
                modifier = Modifier.testTag("checkin_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArloSecondaryContainer,
                    contentColor = ArloPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorderHighlight)
            ) {
                Text("✦ Check in", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    val dialogueBlock = @Composable {
        ArloDialogueBox(
            state = state,
            breakdown = breakdown,
            geminiManager = geminiManager,
            atmosphere = atmosphere,
            onAdvanceGoal = onAdvanceGoal,
            onCompleteMilestone = onCompleteMilestone,
            onOpenGeminiSettings = onOpenGeminiSettings,
            onOpenWeatherSettings = onOpenWeatherSettings,
            onOpenConnectedApps = onOpenConnectedApps
        )
    }

    val peakDashboardBlock = @Composable {
        PeakProductivityDashboard(
            state = state,
            rhythmProfile = rhythmProfile,
            onToggleTask = onToggleTask,
            onAddTask = onAddTask,
            onQuickLogReflection = { mood, note ->
                onCheckIn(3, note, mood)
            },
            onOpenRhythmDetails = onOpenRhythmDetails
        )
    }

    val heroCardBlock = @Composable {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "A GENTLE NUDGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloWarmGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progress beats perfect.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pick one small thing. Arlo will help you keep the promise you make to yourself.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { focusRequester.requestFocus() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add a focus for today", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(ArloPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☼",
                        fontSize = 32.sp,
                        color = ArloWarmGold
                    )
                }
            }
        }
    }

    val tasksBlock = @Composable {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's tasks",
                        style = MaterialTheme.typography.titleLarge,
                        color = ArloTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArloDarkSurfaceVariant
                    ) {
                        Text(
                            text = "$completedTasks/$totalTasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("What would feel good to finish?") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("task_input_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskTitle.isNotBlank()) {
                                    onAddTask(newTaskTitle)
                                    newTaskTitle = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onAddTask(newTaskTitle)
                                newTaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(ArloPrimary, RoundedCornerShape(12.dp))
                            .testTag("add_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add task",
                            tint = ArloOnPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.tasks.isEmpty()) {
                    Text(
                        text = "No tasks yet. Take a moment to set an intention.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.tasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (task.done) ArloDarkSurfaceVariant.copy(alpha = 0.5f) else ArloDarkSurfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.done,
                                    onCheckedChange = { onToggleTask(task.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ArloPrimary,
                                        uncheckedColor = ArloBorderHighlight
                                    ),
                                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (task.done) ArloTextMuted else ArloTextPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onToggleTask(task.id) }
                                )
                                IconButton(
                                    onClick = { onDeleteTask(task.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete task",
                                        tint = ArloTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val reflectionBlock = @Composable {
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
                    text = "REFLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How is your energy right now?",
                    style = MaterialTheme.typography.titleLarge,
                    color = ArloTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moods.forEachIndexed { index, (emoji, desc) ->
                        val isSelected = state.lastMoodIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onCheckIn(index, "", desc) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        if (isSelected) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                                        CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ArloPrimary else ArloBorder,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = if (isSelected) ArloPrimary else ArloTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your check-in is encrypted on this device. Nothing leaves your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArloTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 840.dp

        if (isTablet) {
            // Adaptive Two-Column Layout for Tablets
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1200.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Greeting, Dynamic Peak Dashboard & Dialogue
                LazyColumn(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
                ) {
                    item { headerBlock() }
                    item { peakDashboardBlock() }
                    item { dialogueBlock() }
                }

                // Right Column: Hero Nudge, Focus Tasks & Mood
                LazyColumn(
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
                ) {
                    item { heroCardBlock() }
                    item { tasksBlock() }
                    item { reflectionBlock() }
                }
            }
        } else {
            // Standard Single-Column Layout for Phones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
            ) {
                item { headerBlock() }
                item { peakDashboardBlock() }
                item { dialogueBlock() }
                item { heroCardBlock() }
                item { tasksBlock() }
                item { reflectionBlock() }
            }
        }
    }

    if (showCheckInDialog) {
        DailyCheckInPromptDialog(
            onCompleteCheckIn = { moodIndex, text, label ->
                onCheckIn(moodIndex, text, label)
                showCheckInDialog = false
            },
            onDismiss = { showCheckInDialog = false }
        )
    }
}
