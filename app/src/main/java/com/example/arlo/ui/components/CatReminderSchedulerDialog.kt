package com.example.arlo.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.model.Task
import com.example.arlo.notification.CatNotificationManager
import com.example.arlo.notification.CatTone
import com.example.arlo.notification.ScheduledCatReminder
import com.example.arlo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CatReminderSchedulerDialog(
    tasks: List<Task>,
    notificationManager: CatNotificationManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeReminders by notificationManager.remindersFlow.collectAsState()

    var selectedCategory by remember { mutableStateOf("Task") } // "Task", "Reflection", "Custom"
    var selectedTaskId by remember { mutableStateOf(tasks.firstOrNull { !it.done }?.id ?: "") }
    var customReminderText by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(CatTone.GENTLE_MEOW) }
    var playSound by remember { mutableStateOf(true) }
    var selectedDelayMinutes by remember { mutableLongStateOf(15L) }

    val reflectionTemplates = listOf(
        "Morning Stretch & Intention Setting 🌅",
        "Midday Sunbeam Energy Reset ☀️",
        "Deep Work Biscuit Focus Sprint 🍪",
        "Evening Wind Down & Gratitude Loaf 🌙"
    )
    var selectedReflectionTemplate by remember { mutableStateOf(reflectionTemplates[0]) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission required for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun scheduleCurrentReminder() {
        val targetText = when (selectedCategory) {
            "Task" -> {
                val task = tasks.firstOrNull { it.id == selectedTaskId }
                task?.title ?: "Your sprint task"
            }
            "Reflection" -> selectedReflectionTemplate
            else -> customReminderText.ifBlank { "Mindful Focus Check-in" }
        }

        val title = when (selectedCategory) {
            "Task" -> "Task Reminder: $targetText"
            "Reflection" -> "Mindful Check-in: $targetText"
            else -> "Arlo Reminder: $targetText"
        }

        val delayMillis = selectedDelayMinutes * 60 * 1000L
        notificationManager.scheduleReminder(
            title = title,
            targetTaskOrReflection = targetText,
            delayMillis = delayMillis,
            category = selectedCategory,
            tone = selectedTone,
            playSound = playSound
        )

        Toast.makeText(
            context,
            "🐾 Reminder scheduled for $selectedDelayMinutes minutes! Arlo will meow.",
            Toast.LENGTH_SHORT
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = ArloDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ArloPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cat Reminders & Purrs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🐾", fontSize = 12.sp)
                            }
                            Text(
                                text = "Schedule playful feline notifications & chime alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Category Selector
                    item {
                        Text(
                            text = "What would you like Arlo to remind you about?",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArloPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Task" to "🎯 Active Task", "Reflection" to "🧘 Reflection", "Custom" to "✎ Custom").forEach { (cat, label) ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ArloPrimary else ArloBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCategory = cat }
                                        .testTag("rem_cat_$cat")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Target Configuration
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ArloDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                when (selectedCategory) {
                                    "Task" -> {
                                        Text(
                                            text = "Select Sprint Task:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val pendingTasks = tasks.filter { !it.done }
                                        if (pendingTasks.isEmpty()) {
                                            Text(
                                                text = "No pending tasks in Vault. Add a task or use custom reminder.",
                                                color = ArloTextMuted,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                pendingTasks.forEach { task ->
                                                    val isSelected = selectedTaskId == task.id
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isSelected) ArloPrimaryContainer else ArloDarkSurface,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isSelected) ArloPrimary else ArloBorder
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { selectedTaskId = task.id }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(if (isSelected) "🐾" else "○", fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = task.title,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) ArloPrimary else ArloTextPrimary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "Reflection" -> {
                                        Text(
                                            text = "Choose Reflection Rhythm:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            reflectionTemplates.forEach { template ->
                                                val isSelected = selectedReflectionTemplate == template
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) ArloPrimaryContainer else ArloDarkSurface,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSelected) ArloPrimary else ArloBorder
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedReflectionTemplate = template }
                                                ) {
                                                    Text(
                                                        text = template,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) ArloPrimary else ArloTextPrimary,
                                                        modifier = Modifier.padding(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "Custom" -> {
                                        OutlinedTextField(
                                            value = customReminderText,
                                            onValueChange = { customReminderText = it },
                                            label = { Text("What should Arlo remind you about?") },
                                            placeholder = { Text("e.g. Water plants, Drink water, 15m meditation") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ArloPrimary,
                                                unfocusedBorderColor = ArloBorder,
                                                focusedTextColor = ArloTextPrimary,
                                                unfocusedTextColor = ArloTextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cat Tone Selector
                    item {
                        Text(
                            text = "Choose Playful Feline Notification Tone:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArloPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CatTone.values()) { tone ->
                                val isSelected = selectedTone == tone
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ArloPrimary else ArloBorder
                                    ),
                                    modifier = Modifier.clickable { selectedTone = tone }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(tone.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tone.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Time Delay Selector
                    item {
                        Text(
                            text = "Remind Me in:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArloPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val delays = listOf(
                            1L to "1 min ⚡",
                            5L to "5 min ⏱️",
                            15L to "15 min",
                            30L to "30 min",
                            60L to "1 hour ⏳",
                            120L to "2 hours"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(delays) { (minutes, label) ->
                                val isSelected = selectedDelayMinutes == minutes
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ArloPrimary else ArloBorder
                                    ),
                                    modifier = Modifier.clickable { selectedDelayMinutes = minutes }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloPrimary else ArloTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sound & Instant Test Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = playSound,
                                    onCheckedChange = { playSound = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                                )
                                Text("Play Feline Purr Chime 🎵", color = ArloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    notificationManager.triggerInstantNotification(
                                        title = "🐾 Meow! Arlo Test Check-in",
                                        message = "Purrrrr... Arlo's reminder system is working smoothly!",
                                        tone = selectedTone,
                                        playSound = playSound
                                    )
                                    Toast.makeText(context, "Sent test meow notification!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Test Meow 🐾", fontSize = 11.sp)
                            }
                        }
                    }

                    // Active Scheduled Reminders
                    if (activeReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Scheduled Cat Reminders (${activeReminders.size}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ArloWarmGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeReminders.forEach { reminder ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ArloDarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Text(reminder.tone.icon, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = reminder.title,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ArloTextPrimary
                                                    )
                                                    Text(
                                                        text = "Due at ${formatter.format(Date(reminder.triggerTimeEpochMs))}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ArloTextMuted,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    notificationManager.cancelReminder(reminder.id)
                                                    Toast.makeText(context, "Cancelled reminder", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ArloDanger, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Schedule Button
                Button(
                    onClick = {
                        scheduleCurrentReminder()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_schedule_reminder_button")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Cat Reminder ($selectedDelayMinutes min)", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
