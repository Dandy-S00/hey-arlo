package com.example.arlo.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.model.Goal
import com.example.arlo.ui.theme.*

@Composable
fun GoalsScreen(
    goals: List<Goal>,
    onAddGoal: (String, String) -> Unit,
    onToggleGoal: (String) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

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
                    text = "DIRECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )
                Text(
                    text = "Your goals",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArloTextPrimary
                )
            }
        }

        // Add Goal Card
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
                        text = "Keep the why nearby.",
                        style = MaterialTheme.typography.titleLarge,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Small actions become patterns when they have a reason.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArloTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Goal title") },
                        placeholder = { Text("e.g. Daily mindful movement") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary,
                            focusedLabelColor = ArloPrimary,
                            unfocusedLabelColor = ArloTextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        label = { Text("Why does it matter?") },
                        placeholder = { Text("e.g. To stay clear-headed and energized") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_detail_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (title.isNotBlank()) {
                                    onAddGoal(title, detail)
                                    title = ""
                                    detail = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary,
                            focusedLabelColor = ArloPrimary,
                            unfocusedLabelColor = ArloTextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddGoal(title, detail)
                                title = ""
                                detail = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_goal_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Goals List
        item {
            Text(
                text = "Active intentions (${goals.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary
            )
        }

        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No goals yet. Add one above to anchor your focus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloTextMuted
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_item_${goal.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (goal.done) ArloDarkSurfaceVariant.copy(alpha = 0.5f) else ArloDarkSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleGoal(goal.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (goal.done) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                                    CircleShape
                                )
                        ) {
                            if (goal.done) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = ArloPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "○",
                                    fontSize = 20.sp,
                                    color = ArloTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggleGoal(goal.id) }
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = if (goal.done) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (goal.done) ArloTextMuted else ArloTextPrimary
                            )
                            if (goal.detail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = goal.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (goal.done) ArloTextMuted.copy(alpha = 0.7f) else ArloTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteGoal(goal.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete goal",
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
