package com.example.arlo.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.NoteImportExportManager
import com.example.arlo.data.ParsedNoteImport
import com.example.arlo.model.ApiProvider
import com.example.arlo.model.ConnectionConsent
import com.example.arlo.model.ProviderCatalog
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectorsDialog(
    repository: ArloRepository,
    connections: Map<String, ConnectionConsent>,
    onRequestConnection: (ApiProvider) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val importManager = remember(repository) { NoteImportExportManager(repository) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var pendingConfirmProvider by remember { mutableStateOf<ApiProvider?>(null) }
    var pendingImportParsed by remember { mutableStateOf<ParsedNoteImport?>(null) }
    var importDestination by remember { mutableStateOf("reflection") } // "reflection" or "goal"
    var importMoodOverride by remember { mutableStateOf("Reflective") }
    var activeSourceHint by remember { mutableStateOf("Obsidian") }

    val coroutineScope = rememberCoroutineScope()
    var isCatWorking by remember { mutableStateOf(false) }
    var isCatDone by remember { mutableStateOf(false) }
    var catActionTitle by remember { mutableStateOf("Herding Notes...") }
    var catDoneSaying by remember { mutableStateOf<String?>(null) }

    val state by repository.state.collectAsState()

    // SAF File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var fileName = "note.md"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIdx)
                    }
                }

                val contentStream = context.contentResolver.openInputStream(uri)
                val rawText = contentStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""

                if (rawText.isNotBlank()) {
                    val parsed = importManager.parseContent(rawText, fileName, activeSourceHint)
                    pendingImportParsed = parsed
                } else {
                    Toast.makeText(context, "Selected file was empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val categories = listOf("All", "Note-Taking", "Cloud Storage", "Productivity", "Custom")

    val filteredProviders = remember(searchQuery, selectedCategoryFilter) {
        ProviderCatalog.providers.filter { provider ->
            val matchesCategory = selectedCategoryFilter == "All" || provider.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    provider.name.contains(searchQuery, ignoreCase = true) ||
                    provider.category.contains(searchQuery, ignoreCase = true) ||
                    provider.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            color = ArloDarkSurface,
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "APP CONNECTORS & IMPORTERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary
                        )
                        Text(
                            text = "Cloud & Note-Taking Apps",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloTextPrimary
                        )
                        Text(
                            text = "Connect or import from Obsidian, Notion, Google NotebookLM, Drive, and cloud vaults.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_connectors_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Action Buttons: Import File & Export Markdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            activeSourceHint = "Obsidian / Markdown"
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_import_file_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Note / Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val notes = state?.notes ?: emptyList()
                            val md = importManager.exportReflectionsToObsidianMarkdown(notes)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Obsidian Export", md)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Exported ${notes.size} reflections to clipboard (Obsidian format)", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export to Markdown", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps: Obsidian, Notion, Drive, NotebookLM...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ArloPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ArloTextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connector_search_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary,
                                containerColor = ArloDarkSurfaceVariant,
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

                Spacer(modifier = Modifier.height(8.dp))

                // Provider List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProviders, key = { it.id }) { provider ->
                        val isAwaiting = connections[provider.id]?.status == "awaiting-provider-auth"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connector_row_${provider.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(ArloPrimaryContainer, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = provider.logo,
                                            fontWeight = FontWeight.Black,
                                            color = ArloPrimary,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = provider.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = ArloDarkSurface
                                            ) {
                                                Text(
                                                    text = provider.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ArloTextMuted,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Auth: ${provider.auth}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloPrimary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = provider.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (provider.supportsDirectImport) {
                                        OutlinedButton(
                                            onClick = {
                                                activeSourceHint = provider.name
                                                filePickerLauncher.launch("*/*")
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloPrimary),
                                            modifier = Modifier.testTag("import_btn_${provider.id}")
                                        ) {
                                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Import Notes", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Button(
                                        onClick = {
                                            if (isAwaiting) {
                                                onRequestConnection(provider)
                                            } else {
                                                pendingConfirmProvider = provider
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAwaiting) ArloSecondaryContainer else ArloPrimary,
                                            contentColor = if (isAwaiting) ArloSecondary else ArloOnPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("connect_btn_${provider.id}")
                                    ) {
                                        Text(
                                            text = if (isAwaiting) "Awaiting auth" else "Connect",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = ArloDarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = ArloPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Imported notes from Obsidian, Notion, or cloud vaults are encrypted locally on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // Connect Confirmation Dialog
    if (pendingConfirmProvider != null) {
        val provider = pendingConfirmProvider!!
        AlertDialog(
            onDismissRequest = { pendingConfirmProvider = null },
            title = { Text("Connect to ${provider.name}?") },
            text = {
                Column {
                    Text(
                        "Allow Arlo to configure integration with ${provider.name}?\n\nScope requested:\n• ${provider.description}\n\nSecurity guarantee: Nothing is transferred until you authorize the adapter step.",
                        color = ArloTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestConnection(provider)
                        pendingConfirmProvider = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Approve Connection")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmProvider = null }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }

    // Note Import Preview & Action Dialog
    if (pendingImportParsed != null) {
        val parsed = pendingImportParsed!!
        AlertDialog(
            onDismissRequest = { pendingImportParsed = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = ArloPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Note Preview", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArloDarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Source: ${parsed.sourceApp}", style = MaterialTheme.typography.labelSmall, color = ArloPrimary)
                            Text("File: ${parsed.fileName}", style = MaterialTheme.typography.bodySmall, color = ArloTextSecondary)
                            Text("Title: ${parsed.title}", style = MaterialTheme.typography.titleMedium, color = ArloTextPrimary, fontWeight = FontWeight.Bold)
                            if (parsed.checklistItems.isNotEmpty()) {
                                Text("Checklist items: ${parsed.checklistItems.size} detected", style = MaterialTheme.typography.bodySmall, color = ArloWarmGold)
                            }
                        }
                    }

                    Text("Import destination:", style = MaterialTheme.typography.labelSmall, color = ArloPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = importDestination == "reflection",
                            onClick = { importDestination = "reflection" },
                            label = { Text("Daily Reflection") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = importDestination == "goal",
                            onClick = { importDestination = "goal" },
                            label = { Text("Moving Toward (Intention)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (importDestination == "reflection") {
                        Text("Mood tag:", style = MaterialTheme.typography.labelSmall, color = ArloTextMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("Reflective", "Grateful", "Inspired", "Focused", "Calm")) { m ->
                                FilterChip(
                                    selected = importMoodOverride == m,
                                    onClick = { importMoodOverride = m },
                                    label = { Text(m, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Text("Content preview:", style = MaterialTheme.typography.labelSmall, color = ArloTextMuted)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArloDarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = parsed.body.take(300) + if (parsed.body.length > 300) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedToSave = parsed
                        val destToSave = importDestination
                        val moodToSave = importMoodOverride
                        pendingImportParsed = null
                        isCatWorking = true
                        isCatDone = false
                        catActionTitle = "Importing from ${parsedToSave.sourceApp}..."
                        catDoneSaying = "Purr-fect! Successfully imported into your encrypted vault."
                        coroutineScope.launch {
                            delay(1600)
                            if (destToSave == "reflection") {
                                importManager.importAsReflection(parsedToSave, moodToSave)
                            } else {
                                importManager.importAsGoal(parsedToSave)
                            }
                            isCatWorking = false
                            isCatDone = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Save to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportParsed = null }) {
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
