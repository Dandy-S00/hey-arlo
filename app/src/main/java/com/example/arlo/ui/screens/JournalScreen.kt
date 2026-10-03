package com.example.arlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.model.Note
import com.example.arlo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(
    notes: List<Note>,
    onAddNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    var noteBody by remember { mutableStateOf("") }

    fun formatDate(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(iso) ?: return iso
            val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            iso
        }
    }

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
                    text = "PRIVATE NOTEBOOK",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary
                )
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArloTextPrimary
                )
            }
        }

        // Add Note Card
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
                        text = "Encrypted private reflection",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArloTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteBody,
                        onValueChange = { noteBody = it },
                        placeholder = { Text("What is on your mind? Nothing leaves this device.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .testTag("journal_body_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (noteBody.isNotBlank()) {
                                onAddNote(noteBody)
                                noteBody = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_note_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save private note", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Notes List Header
        item {
            Text(
                text = "Local notes (${notes.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary
            )
        }

        if (notes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Your notes will appear here. Encrypted locally with your vault key.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArloTextMuted
                        )
                    }
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_item_${note.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = formatDate(note.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteNote(note.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete note",
                                    tint = ArloTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
}
