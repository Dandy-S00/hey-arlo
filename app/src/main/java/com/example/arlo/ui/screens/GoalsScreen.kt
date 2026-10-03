package com.example.arlo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.GoalFilterType
import com.example.arlo.data.GoalStateManager
import com.example.arlo.model.Goal
import com.example.arlo.ui.components.CatWorkingDialog
import com.example.arlo.ui.components.RoomCompletionVisualizationCard
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goalStateManager: GoalStateManager,
    repository: ArloRepository? = null
) {
    val goals by goalStateManager.filteredGoalsFlow.collectAsState()
    val metrics by goalStateManager.metricsFlow.collectAsState()
    val filterState by goalStateManager.filterState.collectAsState()

    val roomGoals by (repository?.roomGoals ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    val roomTasks by (repository?.roomTasks ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())

    var showDefineDialog by remember { mutableStateOf(false) }
    var selectedGoalForMilestone by remember { mutableStateOf<String?>(null) }
    var newMilestoneInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var isCatWorking by remember { mutableStateOf(false) }
    var isCatDone by remember { mutableStateOf(false) }
    var catActionTitle by remember { mutableStateOf("Configuring Goal...") }
    var catDoneSaying by remember { mutableStateOf<String?>(null) }

    val categories = GoalStateManager.PRESET_CATEGORIES

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INTENTIONS & DIRECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "What I'm Moving Toward",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArloTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showDefineDialog = true },
                    modifier = Modifier.testTag("define_goal_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ArloOnPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Intention", color = ArloOnPrimary, fontWeight = FontWeight.Black)
                }
            }
        }

        // Room SQLite Completion Over Time Chart
        item {
            RoomCompletionVisualizationCard(
                roomGoals = roomGoals,
                roomTasks = roomTasks
            )
        }

        // Metrics Banner Card
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MOMENTUM & STEPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloWarmGold
                            )
                            Text(
                                text = "${metrics.overallProgressPercent}% Along the Way",
                                style = MaterialTheme.typography.titleLarge,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloPrimaryContainer
                        ) {
                            Text(
                                text = "${metrics.completedCount} of ${metrics.totalCount} celebrated",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = metrics.overallProgressPercent / 100f,
                        label = "progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ArloPrimary,
                        trackColor = ArloDarkSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill("In Motion", "${metrics.inProgressCount}", ArloTextPrimary)
                        StatPill("Celebrated", "${metrics.completedCount}", ArloSuccess)
                        StatPill("Total Items", "${metrics.totalCount}", ArloSecondary)
                    }
                }
            }
        }

        // Filters and Search Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Filter type tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArloDarkSurface, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GoalFilterType.entries.forEach { type ->
                        val isSelected = filterState.filterType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ArloPrimaryContainer else Color.Transparent)
                                .clickable { goalStateManager.setFilterType(type) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ArloPrimary else ArloTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Category chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.selectedCategory == null,
                            onClick = { goalStateManager.setCategoryFilter(null) },
                            label = { Text("All Categories") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary,
                                containerColor = ArloDarkSurface,
                                labelColor = ArloTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterState.selectedCategory == null,
                                borderColor = ArloBorder,
                                selectedBorderColor = ArloPrimary
                            )
                        )
                    }
                    items(categories) { category ->
                        val isSelected = filterState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                goalStateManager.setCategoryFilter(if (isSelected) null else category)
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary,
                                containerColor = ArloDarkSurface,
                                labelColor = ArloTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = ArloBorder,
                                selectedBorderColor = ArloPrimary
                            )
                        )
                    }
                }
            }
        }

        // Goals List Header
        item {
            Text(
                text = "${filterState.filterType.label} (${goals.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary
            )
        }

        // Goals Items
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "◈",
                                fontSize = 32.sp,
                                color = ArloTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (filterState.filterType == GoalFilterType.COMPLETED)
                                    "No celebrated intentions in this view yet."
                                else
                                    "Nothing here yet. Add something you'd like to move toward!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                GoalProgressCard(
                    goal = goal,
                    onToggleComplete = { goalStateManager.toggleComplete(goal.id) },
                    onIncrementProgress = { goalStateManager.incrementProgress(goal.id, 1) },
                    onToggleMilestone = { mId -> goalStateManager.toggleMilestone(goal.id, mId) },
                    onAddMilestoneClick = { selectedGoalForMilestone = goal.id },
                    onDeleteGoal = { goalStateManager.deleteGoal(goal.id) }
                )
            }
        }
    }

    // Define Goal Dialog
    if (showDefineDialog) {
        DefineGoalDialog(
            categories = categories,
            onDismiss = { showDefineDialog = false },
            onSave = { title, detail, category, targetValue, unit, milestones ->
                showDefineDialog = false
                catActionTitle = "Locking In New Goal..."
                catDoneSaying = "Purr-fect! Goal is mapped and locked into your roadmap."
                isCatWorking = true
                isCatDone = false
                coroutineScope.launch {
                    delay(1500)
                    goalStateManager.defineGoal(
                        title = title,
                        detail = detail,
                        category = category,
                        targetValue = targetValue,
                        unit = unit,
                        milestoneTitles = milestones
                    )
                    isCatWorking = false
                    isCatDone = true
                }
            }
        )
    }

    // Add Milestone Dialog
    if (selectedGoalForMilestone != null) {
        AlertDialog(
            onDismissRequest = {
                selectedGoalForMilestone = null
                newMilestoneInput = ""
            },
            title = { Text("Add Milestone Step") },
            text = {
                OutlinedTextField(
                    value = newMilestoneInput,
                    onValueChange = { newMilestoneInput = it },
                    label = { Text("Milestone title") },
                    placeholder = { Text("e.g. Complete module 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMilestoneInput.isNotBlank()) {
                            goalStateManager.addMilestone(selectedGoalForMilestone!!, newMilestoneInput)
                            selectedGoalForMilestone = null
                            newMilestoneInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Add Step")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedGoalForMilestone = null
                    newMilestoneInput = ""
                }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
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

@Composable
private fun StatPill(label: String, value: String, valueColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ArloDarkSurfaceVariant,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = valueColor, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
fun GoalProgressCard(
    goal: Goal,
    onToggleComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onToggleMilestone: (String) -> Unit,
    onAddMilestoneClick: () -> Unit,
    onDeleteGoal: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.done) ArloDarkSurfaceVariant.copy(alpha = 0.55f) else ArloDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (goal.done) ArloPrimaryContainer else ArloBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ArloPrimaryContainer
                ) {
                    Text(
                        text = goal.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.done) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ArloSuccess.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ArloSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completed",
                                    color = ArloSuccess,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDeleteGoal,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = ArloTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox toggle
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (goal.done) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                            CircleShape
                        )
                        .border(1.dp, if (goal.done) ArloPrimary else ArloBorderHighlight, CircleShape)
                        .testTag("toggle_complete_${goal.id}")
                ) {
                    if (goal.done) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = ArloPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("○", fontSize = 18.sp, color = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleComplete() }
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (goal.done) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (goal.done) ArloTextMuted else ArloTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (goal.detail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (goal.done) ArloTextMuted.copy(alpha = 0.7f) else ArloTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar and numeric tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.milestones.isNotEmpty()) {
                        "${goal.milestones.count { it.done }} of ${goal.milestones.size} milestones"
                    } else if (goal.targetValue > 1) {
                        "${goal.currentValue} of ${goal.targetValue} ${goal.unit}s"
                    } else {
                        if (goal.done) "Achieved" else "In progress"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ArloTextSecondary
                )
                Text(
                    text = "${goal.progressPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.done) ArloSuccess else ArloPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (goal.done) ArloSuccess else ArloPrimary,
                trackColor = ArloDarkSurfaceVariant
            )

            // Action row: +1 Quick Increment & Expand Milestones
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (goal.targetValue > 1 && !goal.done) {
                    OutlinedButton(
                        onClick = onIncrementProgress,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloPrimary),
                        modifier = Modifier.testTag("increment_progress_${goal.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 ${goal.unit}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (goal.milestones.isNotEmpty() || !goal.done) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Hide steps" else "Milestones (${goal.milestones.size})",
                            fontSize = 12.sp,
                            color = ArloSecondary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ArloSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Milestones Checklist
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(ArloDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Milestone Checklist",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    goal.milestones.forEach { milestone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = milestone.done,
                                onCheckedChange = { onToggleMilestone(milestone.id) },
                                colors = CheckboxDefaults.colors(checkedColor = ArloPrimary),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (milestone.done) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (milestone.done) ArloTextMuted else ArloTextPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleMilestone(milestone.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onAddMilestoneClick,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArloPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add milestone step", fontSize = 11.sp, color = ArloPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineGoalDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var targetTrackingType by remember { mutableStateOf("simple") } // "simple", "numeric", "milestones"
    var targetCountText by remember { mutableStateOf("7") }
    var unitText by remember { mutableStateOf("day") }
    var milestoneInputs by remember { mutableStateOf(listOf("", "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Something to Move Toward", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are you moving toward? *") },
                    placeholder = { Text("e.g. Daily morning walk & calm thoughts") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_goal_title_input")
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Why does this matter to you? (Your intention)") },
                    placeholder = { Text("e.g. Clears my mind and feels energizing") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary
                            )
                        )
                    }
                }

                Text(
                    text = "Tracking Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = targetTrackingType == "simple",
                        onClick = { targetTrackingType = "simple" },
                        label = { Text("Check-off", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = targetTrackingType == "numeric",
                        onClick = { targetTrackingType = "numeric" },
                        label = { Text("Count", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = targetTrackingType == "milestones",
                        onClick = { targetTrackingType = "milestones" },
                        label = { Text("Milestones", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (targetTrackingType == "numeric") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetCountText,
                            onValueChange = { targetCountText = it },
                            label = { Text("Target count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitText,
                            onValueChange = { unitText = it },
                            label = { Text("Unit") },
                            placeholder = { Text("days, reps") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (targetTrackingType == "milestones") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Milestone steps:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary
                        )
                        milestoneInputs.forEachIndexed { index, mText ->
                            OutlinedTextField(
                                value = mText,
                                onValueChange = { newT ->
                                    val copy = milestoneInputs.toMutableList()
                                    copy[index] = newT
                                    milestoneInputs = copy
                                },
                                placeholder = { Text("Step ${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        TextButton(
                            onClick = { milestoneInputs = milestoneInputs + "" }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add another step")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = when (targetTrackingType) {
                            "numeric" -> targetCountText.toIntOrNull() ?: 1
                            "milestones" -> milestoneInputs.count { it.isNotBlank() }
                            else -> 1
                        }
                        val validMilestones = if (targetTrackingType == "milestones") {
                            milestoneInputs.filter { it.isNotBlank() }
                        } else emptyList()

                        onSave(
                            title,
                            detail,
                            selectedCategory,
                            target,
                            unitText,
                            validMilestones
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                modifier = Modifier.testTag("save_defined_goal_button")
            ) {
                Text("Save Intention")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ArloTextSecondary)
            }
        },
        containerColor = ArloDarkSurface
    )
}
