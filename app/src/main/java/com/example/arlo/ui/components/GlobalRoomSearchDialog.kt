package com.example.arlo.ui.components

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.EmailMessageItem
import com.example.arlo.data.SmsMessageItem
import com.example.arlo.data.db.GoalEntity
import com.example.arlo.data.db.ReflectionEntity
import com.example.arlo.data.db.TaskEntity
import com.example.arlo.model.SavedLink
import com.example.arlo.ui.theme.*

enum class SearchCategoryFilter(val label: String, val icon: String) {
    ALL("All Entities", "🔍"),
    TASKS("Tasks", "⚡"),
    GOALS("Goals", "🎯"),
    REFLECTIONS("Reflections", "✍️"),
    LINKS("Saved Links", "🔖"),
    MESSAGES("SMS & Emails", "📬")
}

sealed class GlobalSearchResultItem {
    data class TaskResult(val entity: TaskEntity) : GlobalSearchResultItem()
    data class GoalResult(val entity: GoalEntity) : GlobalSearchResultItem()
    data class ReflectionResult(val entity: ReflectionEntity) : GlobalSearchResultItem()
    data class LinkResult(val entity: SavedLink) : GlobalSearchResultItem()
    data class SmsResult(val item: SmsMessageItem) : GlobalSearchResultItem()
    data class EmailResult(val item: EmailMessageItem) : GlobalSearchResultItem()
}

@Composable
fun GlobalRoomSearchDialog(
    roomTasks: List<TaskEntity>,
    roomGoals: List<GoalEntity>,
    roomReflections: List<ReflectionEntity>,
    savedLinks: List<SavedLink>,
    smsMessages: List<SmsMessageItem>,
    emails: List<EmailMessageItem>,
    onToggleTask: (String) -> Unit,
    onConvertExternalToTask: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }

    val filteredResults = remember(
        searchQuery,
        selectedFilter,
        roomTasks,
        roomGoals,
        roomReflections,
        savedLinks,
        smsMessages,
        emails
    ) {
        val q = searchQuery.trim().lowercase()
        val results = mutableListOf<GlobalSearchResultItem>()

        // 1. Tasks
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.TASKS) {
            val matchingTasks = if (q.isEmpty()) roomTasks else roomTasks.filter {
                it.title.lowercase().contains(q)
            }
            results.addAll(matchingTasks.map { GlobalSearchResultItem.TaskResult(it) })
        }

        // 2. Goals
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.GOALS) {
            val matchingGoals = if (q.isEmpty()) roomGoals else roomGoals.filter {
                it.title.lowercase().contains(q) || it.detail.lowercase().contains(q) || it.milestonesJson.lowercase().contains(q)
            }
            results.addAll(matchingGoals.map { GlobalSearchResultItem.GoalResult(it) })
        }

        // 3. Reflections
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.REFLECTIONS) {
            val matchingReflections = if (q.isEmpty()) roomReflections else roomReflections.filter {
                it.body.lowercase().contains(q) || it.mood.lowercase().contains(q) || it.prompt.lowercase().contains(q)
            }
            results.addAll(matchingReflections.map { GlobalSearchResultItem.ReflectionResult(it) })
        }

        // 4. Saved Links
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.LINKS) {
            val matchingLinks = if (q.isEmpty()) savedLinks else savedLinks.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q) || it.notes.lowercase().contains(q)
            }
            results.addAll(matchingLinks.map { GlobalSearchResultItem.LinkResult(it) })
        }

        // 5. SMS & Emails
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.MESSAGES) {
            val matchingSms = if (q.isEmpty()) smsMessages else smsMessages.filter {
                it.sender.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
            results.addAll(matchingSms.map { GlobalSearchResultItem.SmsResult(it) })

            val matchingEmails = if (q.isEmpty()) emails else emails.filter {
                it.sender.lowercase().contains(q) || it.subject.lowercase().contains(q) || it.snippet.lowercase().contains(q)
            }
            results.addAll(matchingEmails.map { GlobalSearchResultItem.EmailResult(it) })
        }

        results
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            shape = RoundedCornerShape(24.dp),
            color = ArloDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header & Search Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tasks, goals, notes, sms, emails...", color = ArloTextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = ArloPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ArloTextMuted)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("global_search_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary,
                            focusedContainerColor = ArloDarkSurface,
                            unfocusedContainerColor = ArloDarkSurface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SearchCategoryFilter.entries) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedFilter = filter }
                                .testTag("search_filter_${filter.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(filter.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROOM DATABASE RESULTS (${filteredResults.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "Query: \"$searchQuery\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Results List
                if (filteredResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐱", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "Type above to search stored vault entries" else "No matching entries found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Try searching by keyword, tag, or recipient",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredResults) { item ->
                            when (item) {
                                is GlobalSearchResultItem.TaskResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_task_${item.entity.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = item.entity.done,
                                                onCheckedChange = { onToggleTask(item.entity.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("⚡ TASK", style = MaterialTheme.typography.labelSmall, color = ArloPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    if (item.entity.done) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("✓ COMPLETED", style = MaterialTheme.typography.labelSmall, color = ArloSuccess, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                }
                                                Text(
                                                    text = item.entity.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = ArloTextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                is GlobalSearchResultItem.GoalResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_goal_${item.entity.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🎯 INTENTION / GOAL", style = MaterialTheme.typography.labelSmall, color = ArloSuccess, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                if (item.entity.done) {
                                                    Text("✓ Done", style = MaterialTheme.typography.labelSmall, color = ArloSuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.entity.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (item.entity.detail.isNotBlank()) {
                                                Text(
                                                    text = item.entity.detail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ArloTextSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                is GlobalSearchResultItem.ReflectionResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_reflection_${item.entity.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✍️ REFLECTION • ${item.entity.mood}", style = MaterialTheme.typography.labelSmall, color = ArloWarmGold, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                Text(item.entity.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 9.sp)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.entity.body,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                is GlobalSearchResultItem.LinkResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_link_${item.entity.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("🔖 SAVED RESOURCE", style = MaterialTheme.typography.labelSmall, color = ArloSecondary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.entity.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.entity.url,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                is GlobalSearchResultItem.SmsResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_sms_${item.item.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("💬 SMS • ${item.item.sender}", style = MaterialTheme.typography.labelSmall, color = ArloPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                Text(item.item.dateFormatted, style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 9.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.item.body,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ArloTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { onConvertExternalToTask(item.item.body) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+ Convert to Task", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                is GlobalSearchResultItem.EmailResult -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("result_email_${item.item.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✉️ EMAIL • ${item.item.sender}", style = MaterialTheme.typography.labelSmall, color = ArloWarmGold, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                Text(item.item.dateFormatted, style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 9.sp)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.item.subject,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.item.snippet,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ArloTextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { onConvertExternalToTask(item.item.subject) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+ Convert to Task", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
