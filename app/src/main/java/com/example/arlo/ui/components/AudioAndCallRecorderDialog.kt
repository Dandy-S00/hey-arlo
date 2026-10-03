package com.example.arlo.ui.components

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.AudioStructuredNote
import com.example.arlo.data.GeminiManager
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class AudioRecordMode(val label: String, val icon: String, val description: String) {
    LIVE_AMBIENT("Live Ambient & Meeting", "🎙️", "Capture live room conversations, lectures, or meetings"),
    PHONE_CALL("Phone Call & Conversation", "📞", "Record two-way call conversations and synthesize key takeaways"),
    VOICE_MEMO("Arlo Voice Memo", "🐱", "Quick reflective voice memo for immediate task & note extraction")
}

@Composable
fun AudioAndCallRecorderDialog(
    geminiManager: GeminiManager,
    onDismiss: () -> Unit,
    onSaveNoteToVault: (title: String, body: String, mood: String) -> Unit,
    onAddActionItemsToTasks: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf(AudioRecordMode.LIVE_AMBIENT) }
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var recordDurationSec by remember { mutableIntStateOf(0) }
    var audioOutputFile by remember { mutableStateOf<File?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var resultNote by remember { mutableStateOf<AudioStructuredNote?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    fun startRecording() {
        try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "arlo_record_${System.currentTimeMillis()}.mp4")
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioOutputFile = file
            isRecording = true
            recordDurationSec = 0
            errorMessage = null
            resultNote = null
        } catch (e: Exception) {
            errorMessage = "Could not initialize audio recorder: ${e.message}"
        }
    }

    fun stopRecordingAndTranscribe() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        } catch (e: Exception) {
            isRecording = false
        }

        val file = audioOutputFile
        if (file == null || !file.exists()) {
            errorMessage = "Audio recording file missing"
            return
        }

        isProcessing = true
        scope.launch {
            try {
                val res = geminiManager.transcribeAudioToStructuredNote(
                    audioFile = file,
                    recordingType = selectedMode.label,
                    durationSeconds = recordDurationSec
                )
                res.onSuccess { note ->
                    resultNote = note
                }.onFailure { err ->
                    errorMessage = "Transcription note: ${err.message}"
                }
            } finally {
                isProcessing = false
                try { file.delete() } catch (_: Exception) {}
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            errorMessage = "Microphone permission is required to record audio & calls."
        }
    }

    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordDurationSec++
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isRecording) {
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                } catch (_: Exception) {}
            }
            onDismiss()
        },
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
                            Text("🎙️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Record Audio & Calls to Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "Transcribes speech & auto-extracts action items",
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

                // Mode Selector
                if (!isRecording && resultNote == null) {
                    Text(
                        text = "Select Recording Source:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArloPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioRecordMode.values().forEach { mode ->
                            val isSelected = selectedMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArloPrimary else ArloBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMode = mode }
                                    .testTag("mode_${mode.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(mode.icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
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

                // Main Center Stage: Live Recording / Synthesis / Results
                if (resultNote == null) {
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
                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(waveScale)
                                        .background(ArloDanger.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .background(ArloDanger, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(selectedMode.icon, fontSize = 32.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Recording in Progress...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%02d:%02d", recordDurationSec / 60, recordDurationSec % 60),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArloDanger
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedMode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 11.sp
                                )
                            } else if (isProcessing) {
                                CircularProgressIndicator(color = ArloPrimary, modifier = Modifier.size(50.dp), strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Transcribing & Synthesizing Notes...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Gemini 3.5 is extracting summary, full transcript, and action items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextMuted,
                                    fontSize = 11.sp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(ArloPrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedMode.icon, fontSize = 36.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ready to Record ${selectedMode.label}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedMode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    // Result Note Preview
                    val note = resultNote!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloTextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ArloPrimaryContainer
                                        ) {
                                            Text(
                                                text = note.sentiment,
                                                color = ArloPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Summary:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloPrimary
                                    )
                                    Text(
                                        text = note.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (note.actionItems.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ArloDarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Extracted Action Items (${note.actionItems.size}):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloWarmGold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        note.actionItems.forEach { action ->
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
                                        text = "Verbatim Transcript:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextMuted
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.fullTranscript,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Controls
                if (resultNote == null) {
                    if (isRecording) {
                        Button(
                            onClick = { stopRecordingAndTranscribe() },
                            colors = ButtonDefaults.buttonColors(containerColor = ArloDanger, contentColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop & Transcribe Note", fontWeight = FontWeight.Black)
                        }
                    } else if (!isProcessing) {
                        Button(
                            onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_recording_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Recording ${selectedMode.label}", fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val note = resultNote!!
                                val formattedBody = "${note.summary}\n\nKey Takeaways:\n" +
                                        note.actionItems.joinToString("\n") { "• $it" } +
                                        "\n\nFull Transcript:\n${note.fullTranscript}"
                                onSaveNoteToVault(note.title, formattedBody, note.sentiment)
                                if (note.actionItems.isNotEmpty()) {
                                    onAddActionItemsToTasks(note.actionItems)
                                }
                                Toast.makeText(context, "Saved note & tasks to Vault!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_note_to_vault_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Note to Vault", fontWeight = FontWeight.Black)
                        }

                        OutlinedButton(
                            onClick = { resultNote = null },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("New Recording")
                        }
                    }
                }
            }
        }
    }
}
