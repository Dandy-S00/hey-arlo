package com.example.arlo.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.NaturalRhythmProfile
import com.example.arlo.model.ArloState
import com.example.arlo.model.Task
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

enum class CurrentRhythmPhase(
    val title: String,
    val badgeIcon: String,
    val tagLine: String,
    val accentColor: Color,
    val isPeak: Boolean
) {
    PEAK_FOCUS(
        title = "PEAK FOCUS ZONE ACTIVE",
        badgeIcon = "⚡",
        tagLine = "Your highest cognitive energy window. Tackle your highest-leverage task now.",
        accentColor = Color(0xFFFFD54F),
        isPeak = true
    ),
    PRE_PEAK_WARMUP(
        title = "RAMPING UP TO PEAK",
        badgeIcon = "⏳",
        tagLine = "Approaching peak focus. Clear distractions and prep your key target.",
        accentColor = Color(0xFF81D4FA),
        isPeak = false
    ),
    WIND_DOWN_REST(
        title = "EVENING WIND-DOWN & RESTORATION",
        badgeIcon = "🌙",
        tagLine = "Lower cognitive bandwidth. Shift to gentle reflection and restorative rest.",
        accentColor = Color(0xFFB39DDB),
        isPeak = false
    ),
    STEADY_PACING(
        title = "STEADY FLOW & BALANCED PACING",
        badgeIcon = "🐾",
        tagLine = "Balanced energy. Ideal for steady progress, organizing, and pacing yourself.",
        accentColor = ArloPrimary,
        isPeak = false
    )
}

@Composable
fun PeakProductivityDashboard(
    state: ArloState,
    rhythmProfile: NaturalRhythmProfile,
    onToggleTask: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onQuickLogReflection: (mood: String, note: String) -> Unit,
    onOpenRhythmDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val currentPhase = remember(currentHour, rhythmProfile) {
        val peak = rhythmProfile.peakFocusHour
        val sleep = rhythmProfile.learnedSleepHour
        val prePeak = (peak - 1 + 24) % 24
        val windDownStart = (sleep - 2 + 24) % 24

        when {
            currentHour in peak..(peak + 2) -> CurrentRhythmPhase.PEAK_FOCUS
            currentHour == prePeak -> CurrentRhythmPhase.PRE_PEAK_WARMUP
            currentHour in windDownStart..sleep -> CurrentRhythmPhase.WIND_DOWN_REST
            else -> CurrentRhythmPhase.STEADY_PACING
        }
    }

    val urgentTasks = remember(state.tasks) {
        state.tasks.filter { !it.done }
    }
    val topUrgentTask: Task? = urgentTasks.firstOrNull()

    // 25-minute Pomodoro focus burst state
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSecondsRemaining by remember { mutableIntStateOf(25 * 60) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSecondsRemaining > 0) {
            delay(1000L)
            timerSecondsRemaining -= 1
            if (timerSecondsRemaining <= 0) {
                isTimerRunning = false
                Toast.makeText(context, "🎉 Focus burst completed! Take a 5m breather.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Quick Reflection input
    var reflectionNote by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Steady") }
    var hasLoggedReflectionThisSession by remember { mutableStateOf(false) }

    // Quick task input state
    var newSprintTaskTitle by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("peak_productivity_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (currentPhase.isPeak) currentPhase.accentColor.copy(alpha = pulseAlpha)
            else currentPhase.accentColor.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Dynamic Phase Badge & Why Now Explanation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = currentPhase.accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(currentPhase.badgeIcon, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentPhase.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = currentPhase.accentColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                            if (currentPhase.isPeak) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(currentPhase.accentColor, CircleShape)
                                )
                            }
                        }
                        Text(
                            text = "Peak: ${rhythmProfile.peakHourText} • ${rhythmProfile.chronotype.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ArloDarkSurfaceVariant,
                    modifier = Modifier
                        .clickable { onOpenRhythmDetails() }
                        .testTag("open_rhythm_details_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🐾 Schedule", style = MaterialTheme.typography.labelSmall, color = ArloPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ArloPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentPhase.tagLine,
                style = MaterialTheme.typography.bodySmall,
                color = ArloTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Responsive Content Area (Adaptive Side-by-Side on Tablets vs Stacked on Phones)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 580.dp

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Column: Urgent Task & Focus Burst Timer
                        Column(modifier = Modifier.weight(1.05f)) {
                            UrgentTaskSection(
                                currentPhase = currentPhase,
                                topUrgentTask = topUrgentTask,
                                remainingTasksCount = urgentTasks.size,
                                onToggleTask = onToggleTask,
                                newSprintTaskTitle = newSprintTaskTitle,
                                onNewSprintTaskTitleChange = { newSprintTaskTitle = it },
                                onAddSprintTask = {
                                    if (newSprintTaskTitle.isNotBlank()) {
                                        onAddTask(newSprintTaskTitle)
                                        newSprintTaskTitle = ""
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FocusTimerSection(
                                isTimerRunning = isTimerRunning,
                                timerSecondsRemaining = timerSecondsRemaining,
                                onToggleTimer = { isTimerRunning = !isTimerRunning },
                                onResetTimer = {
                                    isTimerRunning = false
                                    timerSecondsRemaining = 25 * 60
                                }
                            )
                        }

                        // Right Column: Dynamic Energy Reflection
                        Column(modifier = Modifier.weight(0.95f)) {
                            DynamicReflectionSection(
                                currentPhase = currentPhase,
                                selectedMood = selectedMood,
                                onMoodChange = { selectedMood = it },
                                reflectionNote = reflectionNote,
                                onReflectionNoteChange = { reflectionNote = it },
                                hasLogged = hasLoggedReflectionThisSession,
                                onSaveReflection = {
                                    val promptPrefix = when (currentPhase) {
                                        CurrentRhythmPhase.PEAK_FOCUS -> "Peak Focus Reflection"
                                        CurrentRhythmPhase.WIND_DOWN_REST -> "Evening Wind-Down"
                                        CurrentRhythmPhase.PRE_PEAK_WARMUP -> "Pre-Peak Warmup"
                                        CurrentRhythmPhase.STEADY_PACING -> "Steady Pacing Check"
                                    }
                                    onQuickLogReflection(selectedMood, "$promptPrefix: $reflectionNote")
                                    hasLoggedReflectionThisSession = true
                                    Toast.makeText(context, "Reflection saved to vault! ✎", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                } else {
                    // Phone Layout: Stacked Vertically
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UrgentTaskSection(
                            currentPhase = currentPhase,
                            topUrgentTask = topUrgentTask,
                            remainingTasksCount = urgentTasks.size,
                            onToggleTask = onToggleTask,
                            newSprintTaskTitle = newSprintTaskTitle,
                            onNewSprintTaskTitleChange = { newSprintTaskTitle = it },
                            onAddSprintTask = {
                                if (newSprintTaskTitle.isNotBlank()) {
                                    onAddTask(newSprintTaskTitle)
                                    newSprintTaskTitle = ""
                                }
                            }
                        )

                        FocusTimerSection(
                            isTimerRunning = isTimerRunning,
                            timerSecondsRemaining = timerSecondsRemaining,
                            onToggleTimer = { isTimerRunning = !isTimerRunning },
                            onResetTimer = {
                                isTimerRunning = false
                                timerSecondsRemaining = 25 * 60
                            }
                        )

                        DynamicReflectionSection(
                            currentPhase = currentPhase,
                            selectedMood = selectedMood,
                            onMoodChange = { selectedMood = it },
                            reflectionNote = reflectionNote,
                            onReflectionNoteChange = { reflectionNote = it },
                            hasLogged = hasLoggedReflectionThisSession,
                            onSaveReflection = {
                                val promptPrefix = when (currentPhase) {
                                    CurrentRhythmPhase.PEAK_FOCUS -> "Peak Focus Reflection"
                                    CurrentRhythmPhase.WIND_DOWN_REST -> "Evening Wind-Down"
                                    CurrentRhythmPhase.PRE_PEAK_WARMUP -> "Pre-Peak Warmup"
                                    CurrentRhythmPhase.STEADY_PACING -> "Steady Pacing Check"
                                }
                                onQuickLogReflection(selectedMood, "$promptPrefix: $reflectionNote")
                                hasLoggedReflectionThisSession = true
                                Toast.makeText(context, "Reflection saved to vault! ✎", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UrgentTaskSection(
    currentPhase: CurrentRhythmPhase,
    topUrgentTask: Task?,
    remainingTasksCount: Int,
    onToggleTask: (String) -> Unit,
    newSprintTaskTitle: String,
    onNewSprintTaskTitleChange: (String) -> Unit,
    onAddSprintTask: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentPhase.isPeak) "⚡ HIGH-IMPACT SPRINT TARGET" else "🎯 SURFACED TASK PRIORITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = currentPhase.accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
                if (remainingTasksCount > 1) {
                    Text(
                        text = "+${remainingTasksCount - 1} more queued",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (topUrgentTask != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentPhase.accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTask(topUrgentTask.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = topUrgentTask.done,
                            onCheckedChange = { onToggleTask(topUrgentTask.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = currentPhase.accentColor,
                                uncheckedColor = ArloBorderHighlight
                            ),
                            modifier = Modifier.testTag("urgent_task_checkbox_${topUrgentTask.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topUrgentTask.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (topUrgentTask.done) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = ArloTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentPhase.isPeak) "Single focus target for current peak window" else "Next intention to complete",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                // Inline Add Target Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSprintTaskTitle,
                        onValueChange = onNewSprintTaskTitleChange,
                        placeholder = { Text("What is your single focus target?", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sprint_target_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentPhase.accentColor,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAddSprintTask,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentPhase.accentColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Set", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimerSection(
    isTimerRunning: Boolean,
    timerSecondsRemaining: Int,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit
) {
    val minutes = timerSecondsRemaining / 60
    val seconds = timerSecondsRemaining % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val progress = (25 * 60 - timerSecondsRemaining).toFloat() / (25 * 60)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ArloDarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isTimerRunning) ArloPrimaryContainer else ArloDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(34.dp),
                        color = ArloWarmGold,
                        strokeWidth = 3.dp,
                        trackColor = ArloBorder
                    )
                    Text(if (isTimerRunning) "⚡" else "⏱️", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isTimerRunning) ArloWarmGold else ArloTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isTimerRunning) "25m Sprint Active" else "25m Ultradian Focus Burst",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onToggleTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) ArloWarmGold else ArloPrimary,
                        contentColor = if (isTimerRunning) Color.Black else ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_focus_timer_button")
                ) {
                    Text(if (isTimerRunning) "Pause" else "Start Burst", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (isTimerRunning || timerSecondsRemaining < 25 * 60) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onResetTimer, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", tint = ArloTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicReflectionSection(
    currentPhase: CurrentRhythmPhase,
    selectedMood: String,
    onMoodChange: (String) -> Unit,
    reflectionNote: String,
    onReflectionNoteChange: (String) -> Unit,
    hasLogged: Boolean,
    onSaveReflection: () -> Unit
) {
    val moods = listOf(
        Pair("😵", "Exhausted"),
        Pair("😕", "Low"),
        Pair("😐", "Steady"),
        Pair("🙂", "Good"),
        Pair("✨", "Inspired")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentPhase) {
                        CurrentRhythmPhase.PEAK_FOCUS -> "⚡ FLOW CALIBRATION REFLECTION"
                        CurrentRhythmPhase.WIND_DOWN_REST -> "🌙 EVENING RESTORATION REFLECTION"
                        CurrentRhythmPhase.PRE_PEAK_WARMUP -> "⏳ INTENTION READINESS CHECK"
                        CurrentRhythmPhase.STEADY_PACING -> "✎ RHYTHM MICRO-CHECKIN"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = currentPhase.accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )

                if (hasLogged) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ArloSuccess.copy(alpha = 0.2f)
                    ) {
                        Text("✓ Saved", color = ArloSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mood Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moods) { (emoji, desc) ->
                    val isSelected = selectedMood.equals(desc, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) currentPhase.accentColor.copy(alpha = 0.2f) else ArloDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) currentPhase.accentColor else ArloBorder
                        ),
                        modifier = Modifier
                            .clickable { onMoodChange(desc) }
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) currentPhase.accentColor else ArloTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note Input Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = onReflectionNoteChange,
                    placeholder = {
                        Text(
                            text = when (currentPhase) {
                                CurrentRhythmPhase.PEAK_FOCUS -> "What will make this focus session successful?"
                                CurrentRhythmPhase.WIND_DOWN_REST -> "What brought peace or progress today?"
                                else -> "How is your energy right now?"
                            },
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dynamic_reflection_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentPhase.accentColor,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onSaveReflection,
                    modifier = Modifier
                        .size(42.dp)
                        .background(currentPhase.accentColor, RoundedCornerShape(10.dp))
                        .testTag("save_dynamic_reflection_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save reflection",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
