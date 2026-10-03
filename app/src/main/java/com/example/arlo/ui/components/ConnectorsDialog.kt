package com.example.arlo.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.arlo.model.SyncResultSummary
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConnectorsDialog(
    repository: ArloRepository,
    connections: Map<String, ConnectionConsent>,
    onRequestConnection: (ApiProvider) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val importManager = remember(repository) { NoteImportExportManager(repository) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var isSyncingAll by remember { mutableStateOf(false) }
    var syncingProviderName by remember { mutableStateOf<String?>(null) }
    var syncProgress by remember { mutableFloatStateOf(0f) }
    var lastSyncSummary by remember { mutableStateOf<SyncResultSummary?>(null) }
    var syncingSingleProviderId by remember { mutableStateOf<String?>(null) }

    var pendingImportParsed by remember { mutableStateOf<ParsedNoteImport?>(null) }
    var importDestination by remember { mutableStateOf("reflection") }
    var activeSourceHint by remember { mutableStateOf("Obsidian") }

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

    val categories = listOf("All", "Connected", "Note-Taking", "Cloud Storage", "Productivity", "Communication")

    val filteredProviders = remember(searchQuery, selectedCategoryFilter, connections) {
        ProviderCatalog.providers.filter { provider ->
            val isConnected = connections[provider.id] != null
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Connected" -> isConnected
                else -> provider.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
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
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
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
                            Text("🔄", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Integrations & Sync Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "${connections.size} active integrations connected",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Master "Sync All Integrations" Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sync All Connected Integrations",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Text(
                                    text = if (isSyncingAll) "Syncing $syncingProviderName..." else "Reconcile notes, tasks, calendar & messages in 1 tap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSyncingAll) ArloPrimary else ArloTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    isSyncingAll = true
                                    syncProgress = 0.1f
                                    scope.launch {
                                        val summary = repository.syncAllIntegrations { providerName, progress ->
                                            syncingProviderName = providerName
                                            syncProgress = progress
                                        }
                                        lastSyncSummary = summary
                                        isSyncingAll = false
                                        syncingProviderName = null
                                        Toast.makeText(context, "All integrations synchronized!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isSyncingAll,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArloPrimary,
                                    contentColor = ArloOnPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("sync_all_integrations_master_button")
                            ) {
                                if (isSyncingAll) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = ArloOnPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync All", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (isSyncingAll) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { syncProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ArloPrimary,
                                trackColor = ArloDarkSurface
                            )
                        }

                        if (lastSyncSummary != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Last Synced: ${lastSyncSummary?.timestamp} • ${lastSyncSummary?.syncedItemsCount} items reconciled",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search and Category Chips
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter integrations (Obsidian, Notion, Calendar...)", color = ArloTextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArloPrimary) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
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

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Cards List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredProviders) { provider ->
                        val consent = connections[provider.id]
                        val isConnected = consent != null
                        val isSyncingThis = syncingSingleProviderId == provider.id

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ArloDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isConnected) ArloPrimary.copy(alpha = 0.5f) else ArloBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("provider_card_${provider.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(ArloDarkSurface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(provider.logo, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = provider.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArloTextPrimary
                                                )
                                                if (isConnected) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = ArloSuccess.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "✓ SYNCED",
                                                            color = ArloSuccess,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = provider.syncTypeDescription,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // Connect / Disconnect Toggle Button
                                    Button(
                                        onClick = {
                                            if (isConnected) {
                                                repository.disconnectProvider(provider.id)
                                                Toast.makeText(context, "Disconnected ${provider.name}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                repository.connectProvider(provider)
                                                Toast.makeText(context, "Connected & Synced ${provider.name}!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isConnected) ArloDarkSurface else ArloPrimary,
                                            contentColor = if (isConnected) ArloDanger else ArloOnPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(32.dp).testTag("connect_btn_${provider.id}")
                                    ) {
                                        Text(
                                            text = if (isConnected) "Disconnect" else "Connect",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = provider.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                if (isConnected) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Last Synced: ${consent.lastSyncAt?.take(16) ?: "Just now"} (${consent.itemsImportedCount} items)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted,
                                            fontSize = 10.sp
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (provider.supportsDirectImport) {
                                                Button(
                                                    onClick = {
                                                        activeSourceHint = provider.name
                                                        filePickerLauncher.launch("*/*")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Import File", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    syncingSingleProviderId = provider.id
                                                    scope.launch {
                                                        val res = repository.syncProvider(provider.id)
                                                        syncingSingleProviderId = null
                                                        Toast.makeText(context, "${res.providerName} synchronized!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                enabled = !isSyncingThis,
                                                colors = ButtonDefaults.buttonColors(containerColor = ArloPrimaryContainer, contentColor = ArloPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp).testTag("sync_single_${provider.id}")
                                            ) {
                                                if (isSyncingThis) {
                                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ArloPrimary, strokeWidth = 2.dp)
                                                } else {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Black)
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

    // Direct Note Import Review Dialog
    if (pendingImportParsed != null) {
        val parsed = pendingImportParsed!!
        AlertDialog(
            onDismissRequest = { pendingImportParsed = null },
            title = {
                Text("Import to Vault (${parsed.sourceApp})", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "File: ${parsed.fileName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = parsed.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = parsed.body.take(160) + if (parsed.body.length > 160) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        importManager.importAsReflection(parsed)
                        pendingImportParsed = null
                        Toast.makeText(context, "Imported into encrypted vault!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Save to Vault", fontWeight = FontWeight.Black)
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
}
