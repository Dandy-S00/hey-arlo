package com.example.arlo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.model.ArloState
import com.example.arlo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    state: ArloState,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onCheckIn: (Int) -> Unit,
    onOpenConnectors: () -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showCheckInDialog by remember { mutableStateOf(false) }

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
                        text = dateString.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary
                    )
                    Text(
                        text = "$greeting, you.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ArloTextPrimary
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

        // Hero Card
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

        // Tasks Card
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

        // Reflection Card
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
                                    .clickable { onCheckIn(index) }
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
    }

    if (showCheckInDialog) {
        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✦ Daily Check-in")
                }
            },
            text = {
                Column {
                    Text(
                        "How is your mind and body feeling today?",
                        color = ArloTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        moods.forEachIndexed { index, (emoji, label) ->
                            Button(
                                onClick = {
                                    onCheckIn(index)
                                    showCheckInDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.lastMoodIndex == index) ArloPrimaryContainer else ArloDarkSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(emoji, fontSize = 20.sp)
                                    Text(label, fontSize = 10.sp, color = ArloTextPrimary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = ArloDarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 What is one kind thing you can do for yourself today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloWarmGold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text("Close", color = ArloPrimary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
