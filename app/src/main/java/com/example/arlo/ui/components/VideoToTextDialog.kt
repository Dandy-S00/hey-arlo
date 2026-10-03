package com.example.arlo.ui.components

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.VideoTranscriptionResult
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VideoToTextDialog(
    geminiManager: GeminiManager,
    onDismiss: () -> Unit,
    onSaveNoteToVault: (title: String, body: String, mood: String) -> Unit,
    onSaveLinkToResources: (url: String, title: String, notes: String, tags: List<String>) -> Unit,
    onAddActionItemsToTasks: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var inputUrl by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultVideo by remember { mutableStateOf<VideoTranscriptionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sampleLinks = listOf(
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "YouTube",
        "https://www.loom.com/share/sample-product-demo" to "Loom",
        "https://vimeo.com/76979871" to "Vimeo Lecture"
    )

    fun processVideoUrl() {
        val url = inputUrl.trim()
        if (url.isBlank()) {
            errorMessage = "Please enter a valid video link"
            return
        }

        isProcessing = true
        errorMessage = null
        resultVideo = null

        scope.launch {
            try {
                val res = geminiManager.transcribeVideoLinkToText(url)
                res.onSuccess { videoData ->
                    resultVideo = videoData
                }.onFailure { err ->
                    errorMessage = "Could not retrieve video transcript: ${err.message}"
                }
            } finally {
                isProcessing = false
            }
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
                            Text("📹", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Video to Text & Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "Retrieve transcript & summary from video URLs",
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

                // Input Bar
                if (resultVideo == null) {
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = {
                            inputUrl = it
                            errorMessage = null
                        },
                        placeholder = { Text("Paste YouTube, Loom, Vimeo or video URL...", color = ArloTextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_url_input_field"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { processVideoUrl() }),
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = ArloPrimary)
                        },
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

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sampleLinks) { (sample, name) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                modifier = Modifier.clickable { inputUrl = sample }
                            ) {
                                Text(
                                    text = "+ Try $name link",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloTextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArloDangerContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ArloDanger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Processing / Results Section
                if (isProcessing) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = ArloPrimary, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Retrieving Video & Transcribing Speech...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Fetching metadata, extracting audio chapters, and structuring insights",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else if (resultVideo != null) {
                    val video = resultVideo!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
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
                                                text = video.channelOrSource,
                                                color = ArloPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Text(
                                            text = "⏱️ ${video.estimatedDuration}",
                                            color = ArloTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = video.videoTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = video.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (video.keyTakeaways.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ArloDarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Key Takeaways & Frameworks:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        video.keyTakeaways.forEach { takeaway ->
                                            Text(
                                                text = "💡 $takeaway",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ArloTextPrimary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (video.actionItems.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ArloDarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Actionable Steps from Video:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloWarmGold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        video.actionItems.forEach { action ->
                                            Text(
                                                text = "• $action",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ArloTextPrimary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ArloDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Full Speech Transcript:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextMuted
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = video.fullTranscript,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(ArloPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📹", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Enter a Video Link Above",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Arlo will retrieve video details, transcribe spoken audio, and format notes for your vault",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Actions
                if (resultVideo == null) {
                    Button(
                        onClick = { processVideoUrl() },
                        enabled = inputUrl.isNotBlank() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("retrieve_video_notes_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retrieve & Transcribe Video", fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val video = resultVideo!!
                                val formattedBody = "${video.summary}\n\nKey Takeaways:\n" +
                                        video.keyTakeaways.joinToString("\n") { "💡 $it" } +
                                        "\n\nAction Items:\n" +
                                        video.actionItems.joinToString("\n") { "• $it" } +
                                        "\n\nFull Transcript:\n${video.fullTranscript}"
                                onSaveNoteToVault(video.videoTitle, formattedBody, "Learning")
                                onSaveLinkToResources(video.videoUrl, video.videoTitle, video.summary, video.tags)
                                if (video.actionItems.isNotEmpty()) {
                                    onAddActionItemsToTasks(video.actionItems)
                                }
                                Toast.makeText(context, "Saved video notes & link to Vault!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_video_to_vault_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Vault Notes", fontWeight = FontWeight.Black)
                        }

                        OutlinedButton(
                            onClick = {
                                resultVideo = null
                                inputUrl = ""
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("New Video")
                        }
                    }
                }
            }
        }
    }
}
