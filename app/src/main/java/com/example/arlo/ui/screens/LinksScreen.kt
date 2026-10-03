package com.example.arlo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.R
import com.example.arlo.model.SavedLink
import com.example.arlo.ui.theme.*

@Composable
fun LinksScreen(
    savedLinks: List<SavedLink>,
    onAddLink: (url: String, title: String, notes: String, tags: List<String>, isFavorite: Boolean) -> Unit,
    onUpdateLink: (SavedLink) -> Unit,
    onDeleteLink: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleRead: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "favorites", "unread", "read", or a specific tag
    var showAddDialog by remember { mutableStateOf(false) }
    var linkToEdit by remember { mutableStateOf<SavedLink?>(null) }

    // Aggregate tags
    val allTags = remember(savedLinks) {
        savedLinks.flatMap { it.tags }.distinct().sorted()
    }

    val filteredLinks = remember(savedLinks, searchQuery, selectedFilter) {
        savedLinks.filter { link ->
            val matchesSearch = searchQuery.isBlank() ||
                    link.title.contains(searchQuery, ignoreCase = true) ||
                    link.url.contains(searchQuery, ignoreCase = true) ||
                    link.notes.contains(searchQuery, ignoreCase = true) ||
                    link.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesFilter = when (selectedFilter) {
                "all" -> true
                "favorites" -> link.isFavorite
                "unread" -> !link.isRead
                "read" -> link.isRead
                else -> link.tags.any { it.equals(selectedFilter, ignoreCase = true) }
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ArloPrimary,
                contentColor = ArloOnPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_link_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Link")
            }
        },
        containerColor = ArloDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ArloPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔖", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Linksi Vault",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Save links & keep private notes in your encrypted vault",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloDarkSurfaceVariant
                ) {
                    Text(
                        text = "${savedLinks.size} saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search links, notes, tags, or domains...", fontSize = 12.sp, color = ArloTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArloTextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = ArloTextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_links_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArloPrimary,
                    unfocusedBorderColor = ArloBorder,
                    focusedTextColor = ArloTextPrimary,
                    unfocusedTextColor = ArloTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("All (${savedLinks.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArloPrimaryContainer,
                            selectedLabelColor = ArloPrimary
                        )
                    )
                }
                item {
                    val favCount = savedLinks.count { it.isFavorite }
                    FilterChip(
                        selected = selectedFilter == "favorites",
                        onClick = { selectedFilter = "favorites" },
                        label = { Text("⭐ Favorites ($favCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArloPrimaryContainer,
                            selectedLabelColor = ArloWarmGold
                        )
                    )
                }
                item {
                    val unreadCount = savedLinks.count { !it.isRead }
                    FilterChip(
                        selected = selectedFilter == "unread",
                        onClick = { selectedFilter = "unread" },
                        label = { Text("📖 Unread ($unreadCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArloPrimaryContainer,
                            selectedLabelColor = ArloPrimary
                        )
                    )
                }
                items(allTags) { tag ->
                    val tagCount = savedLinks.count { it.tags.any { t -> t.equals(tag, ignoreCase = true) } }
                    FilterChip(
                        selected = selectedFilter.equals(tag, ignoreCase = true),
                        onClick = {
                            selectedFilter = if (selectedFilter.equals(tag, ignoreCase = true)) "all" else tag
                        },
                        label = { Text("#$tag ($tagCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArloPrimaryContainer,
                            selectedLabelColor = ArloPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Links List
            if (filteredLinks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐾", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No links in this view",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to save a link and jot private notes on it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(filteredLinks, key = { it.id }) { link ->
                        SavedLinkCard(
                            link = link,
                            onOpenLink = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyLink = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Link", link.url))
                                Toast.makeText(context, "Copied URL to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onToggleFavorite = { onToggleFavorite(link.id) },
                            onToggleRead = { onToggleRead(link.id) },
                            onEdit = { linkToEdit = link },
                            onDelete = { onDeleteLink(link.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Link Dialog
    if (showAddDialog) {
        SaveLinkDialog(
            initialLink = null,
            onSave = { url, title, notes, tags, isFavorite ->
                onAddLink(url, title, notes, tags, isFavorite)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Link Dialog
    linkToEdit?.let { link ->
        SaveLinkDialog(
            initialLink = link,
            onSave = { url, title, notes, tags, isFavorite ->
                onUpdateLink(
                    link.copy(
                        url = url,
                        title = title,
                        notes = notes,
                        tags = tags,
                        isFavorite = isFavorite
                    )
                )
                linkToEdit = null
            },
            onDismiss = { linkToEdit = null }
        )
    }
}

@Composable
fun SavedLinkCard(
    link: SavedLink,
    onOpenLink: () -> Unit,
    onCopyLink: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_link_card_${link.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (link.isFavorite) ArloWarmGold.copy(alpha = 0.5f) else ArloBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ArloPrimaryContainer
                        ) {
                            Text(
                                text = link.domain,
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (link.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ArloSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Read ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloSuccess,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = link.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = link.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(if (link.isFavorite) "⭐" else "☆", fontSize = 16.sp)
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ArloTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Notes Section (Linksi Highlight)
            if (link.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ArloDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Personal Notes",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloWarmGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = link.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            lineHeight = 17.sp,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Tags row
            if (link.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(link.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ArloDarkSurface
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onOpenLink) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArloPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = ArloPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onCopyLink) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArloTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = ArloTextSecondary, fontSize = 12.sp)
                    }

                    TextButton(onClick = onToggleRead) {
                        Text(
                            text = if (link.isRead) "Mark Unread" else "Mark Read",
                            color = ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ArloDanger.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SaveLinkDialog(
    initialLink: SavedLink?,
    onSave: (url: String, title: String, notes: String, tags: List<String>, isFavorite: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var urlInput by remember { mutableStateOf(initialLink?.url ?: "") }
    var titleInput by remember { mutableStateOf(initialLink?.title ?: "") }
    var notesInput by remember { mutableStateOf(initialLink?.notes ?: "") }
    var tagsInput by remember { mutableStateOf(initialLink?.tags?.joinToString(", ") ?: "") }
    var isFavorite by remember { mutableStateOf(initialLink?.isFavorite ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .testTag("save_link_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialLink != null) "Edit Saved Link" else "Save Link to Vault",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // URL Field
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Web URL (Required)", fontSize = 11.sp) },
                    placeholder = { Text("https://example.com/article", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("link_url_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Title Field
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title / Bookmark Name (Optional)", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Clean Architecture Guide", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("link_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Personal Notes Field
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes & Thoughts (Linksi)", fontSize = 11.sp) },
                    placeholder = { Text("Key insights, why you saved it, takeaways...", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .testTag("link_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tags Field
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma-separated)", fontSize = 11.sp) },
                    placeholder = { Text("Tech, Reading, Inspiration", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Favorite Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFavorite = !isFavorite },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it },
                        colors = CheckboxDefaults.colors(checkedColor = ArloWarmGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Favorite ⭐", style = MaterialTheme.typography.bodyMedium, color = ArloTextPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedTags = tagsInput.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        onSave(urlInput, titleInput, notesInput, parsedTags, isFavorite)
                    },
                    enabled = urlInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_link_confirm_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save to Vault", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
