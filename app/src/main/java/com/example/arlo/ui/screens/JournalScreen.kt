package com.example.arlo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.JournalStateManager
import com.example.arlo.model.Note
import com.example.arlo.ui.components.CatWorkingDialog
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    journalStateManager: JournalStateManager
) {
    val context = LocalContext.current
    val reflections by journalStateManager.filteredReflectionsFlow.collectAsState()
    val metrics by journalStateManager.metricsFlow.collectAsState()
    val filterState by journalStateManager.filterState.collectAsState()

    var reflectionBody by remember { mutableStateOf("") }
    var selectedPrompt by remember { mutableStateOf(JournalStateManager.PROMPT_PRESETS.first()) }
    var selectedMood by remember { mutableStateOf(JournalStateManager.MOOD_TAGS.first().first) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var editBodyInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var isCatWorking by remember { mutableStateOf(false) }
    var isCatDone by remember { mutableStateOf(false) }

    val currentTimestampStr = remember {
        val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
        formatter.format(Date())
    }

    fun formatDisplayDate(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(iso) ?: return iso
            val formatter = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            iso
        }
    }

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
                        text = "DAILY REFLECTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary
                    )
                    Text(
                        text = "Journaling",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ArloTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloDarkSurfaceVariant
                ) {
                    Text(
                        text = "🔒 Local Vault",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Metrics Banner Card
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.totalEntries}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Stored Reflections",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(ArloBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.uniqueDaysCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloWarmGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Days Journaled",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(ArloBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = metrics.mostCommonMood,
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Primary Mood",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted
                        )
                    }
                }
            }
        }

        // Daily Reflection Composer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorderHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "New Reflection",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Guiding Prompt:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted
                    )

                    // Prompt selector chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(JournalStateManager.PROMPT_PRESETS) { prompt ->
                            val isSelected = selectedPrompt == prompt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPrompt = prompt },
                                label = { Text(prompt, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ArloPrimaryContainer,
                                    selectedLabelColor = ArloPrimary,
                                    containerColor = ArloDarkSurfaceVariant,
                                    labelColor = ArloTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Energy / Mood:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted
                    )

                    // Mood Selector Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(JournalStateManager.MOOD_TAGS) { (mood, emoji) ->
                            val isSelected = selectedMood == mood
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMood = mood },
                                label = { Text("$emoji $mood", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ArloPrimaryContainer,
                                    selectedLabelColor = ArloPrimary,
                                    containerColor = ArloDarkSurfaceVariant,
                                    labelColor = ArloTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reflectionBody,
                        onValueChange = { reflectionBody = it },
                        placeholder = {
                            Text("What is in your thoughts? Reflect freely; your writing is encrypted and stays entirely on this device.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .testTag("reflection_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timestamp indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ArloPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentTimestampStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${reflectionBody.split("\\s+".toRegex()).count { it.isNotBlank() }} words",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (reflectionBody.isNotBlank()) {
                                val bodyToSave = reflectionBody
                                val promptToSave = selectedPrompt
                                val moodToSave = selectedMood
                                reflectionBody = ""
                                isCatWorking = true
                                isCatDone = false
                                coroutineScope.launch {
                                    delay(1600)
                                    journalStateManager.saveReflection(
                                        body = bodyToSave,
                                        prompt = promptToSave,
                                        mood = moodToSave
                                    )
                                    isCatWorking = false
                                    isCatDone = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_reflection_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Store Daily Reflection", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { journalStateManager.setSearchQuery(it) },
                placeholder = { Text("Search your past reflections...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = ArloPrimary)
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { journalStateManager.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = ArloTextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArloPrimary,
                    unfocusedBorderColor = ArloBorder,
                    focusedTextColor = ArloTextPrimary,
                    unfocusedTextColor = ArloTextPrimary
                )
            )
        }

        // Reflections Feed Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reflections Timeline (${reflections.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArloTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                if (filterState.selectedMood != null || filterState.searchQuery.isNotEmpty()) {
                    TextButton(onClick = { journalStateManager.clearFilters() }) {
                        Text("Clear filter", color = ArloPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Reflections Feed Cards
        if (reflections.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "✎", fontSize = 32.sp, color = ArloTextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (filterState.searchQuery.isNotEmpty())
                                    "No reflections matched your search."
                                else
                                    "No reflections stored yet. Write down your first thought above to begin your daily journal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(reflections, key = { it.id }) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reflection_card_${note.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Card Header: Timestamp & Mood
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = ArloPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatDisplayDate(note.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (note.mood.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ArloPrimaryContainer
                                    ) {
                                        Text(
                                            text = note.mood,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = {
                                        editingNote = note
                                        editBodyInput = note.body
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit reflection",
                                        tint = ArloTextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Reflection", note.body)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy text",
                                        tint = ArloTextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { journalStateManager.deleteReflection(note.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete reflection",
                                        tint = ArloTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Prompt Header if present
                        if (note.prompt.isNotBlank() && note.prompt != "Free-form reflection") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ArloDarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Prompt: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloWarmGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = note.prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reflection Body Text
                        Text(
                            text = note.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ArloTextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }

    // Edit Reflection Dialog
    if (editingNote != null) {
        val note = editingNote!!
        AlertDialog(
            onDismissRequest = { editingNote = null },
            title = { Text("Edit Reflection") },
            text = {
                Column {
                    Text(
                        text = "Original timestamp: ${formatDisplayDate(note.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editBodyInput,
                        onValueChange = { editBodyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editBodyInput.isNotBlank()) {
                            journalStateManager.updateReflection(note.id, editBodyInput, note.mood)
                            editingNote = null
                            Toast.makeText(context, "Reflection updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNote = null }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }

    if (isCatWorking || isCatDone) {
        CatWorkingDialog(
            isWorking = isCatWorking,
            actionTitle = "Encrypting & Storing Reflection...",
            isDone = isCatDone,
            customDoneSaying = "Purr-fect! Your daily reflection is safely encrypted in the vault.",
            onDismiss = {
                isCatWorking = false
                isCatDone = false
            }
        )
    }
}
