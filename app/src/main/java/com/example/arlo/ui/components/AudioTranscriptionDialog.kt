package com.example.arlo.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.core.content.ContextCompat
import com.example.arlo.data.GeminiManager
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AudioTranscriptionDialog(
    geminiManager: GeminiManager,
    onDismiss: () -> Unit,
    onTranscribedText: (String, destination: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var transcribedResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recordDurationSec by remember { mutableIntStateOf(0) }
    var selectedDestination by remember { mutableStateOf("task") } // "task", "goal", "journal"

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioOutputFile by remember { mutableStateOf<File?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startAudioRecording(
                context = context,
                onStart = { recorder, file ->
                    mediaRecorder = recorder
                    audioOutputFile = file
                    isRecording = true
                    recordDurationSec = 0
                },
                onError = { errorMessage = it }
            )
        } else {
            errorMessage = "Microphone permission is required to record voice notes."
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

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                stopAudioRecording(mediaRecorder)
            }
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ArloPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎙️", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Voice Transcription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArloTextPrimary
                    )
                    Text(
                        text = "Powered by Gemini 3.5 Transcribe",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArloDangerContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
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

                // Destination Selector Chips
                Text(
                    text = "Insert transcription as:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloTextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("task", "⚡ Task", "task"),
                        Triple("goal", "🎯 Goal", "goal"),
                        Triple("journal", "✍️ Reflection", "journal")
                    ).forEach { (key, label, _) ->
                        val isSelected = selectedDestination == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDestination = key }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Microphone Button
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(if (isRecording) ArloDanger else ArloPrimary)
                        .clickable {
                            if (isRecording) {
                                // Stop and transcribe
                                val file = audioOutputFile
                                stopAudioRecording(mediaRecorder)
                                isRecording = false
                                mediaRecorder = null

                                if (file != null && file.exists()) {
                                    isTranscribing = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            val audioBytes = file.readBytes()
                                            val res = geminiManager.transcribeAudio(audioBytes, "audio/mp4")
                                            res.onSuccess { text ->
                                                transcribedResult = text
                                            }.onFailure { err ->
                                                errorMessage = err.message ?: "Failed to transcribe audio."
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Error processing audio file."
                                        } finally {
                                            isTranscribing = false
                                        }
                                    }
                                }
                            } else {
                                // Request permission & start
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startAudioRecording(
                                        context = context,
                                        onStart = { recorder, file ->
                                            mediaRecorder = recorder
                                            audioOutputFile = file
                                            isRecording = true
                                            recordDurationSec = 0
                                            transcribedResult = null
                                            errorMessage = null
                                        },
                                        onError = { errorMessage = it }
                                    )
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("microphone_record_action"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) Color.White else ArloOnPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        isRecording -> "Recording... (${recordDurationSec}s) Tap to Stop"
                        isTranscribing -> "Transcribing with Gemini 3.5 Transcribe..."
                        transcribedResult != null -> "Transcription Complete!"
                        else -> "Tap microphone to record voice note"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRecording) ArloDanger else ArloTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                // Transcribed Text Result Box
                if (transcribedResult != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Transcribed Text:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = transcribedResult ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (transcribedResult != null) {
                Button(
                    onClick = {
                        transcribedResult?.let { text ->
                            onTranscribedText(text, selectedDestination)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_transcription_button")
                ) {
                    Text("Save to $selectedDestination", fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ArloTextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = ArloDarkSurface
    )
}

private fun startAudioRecording(
    context: Context,
    onStart: (MediaRecorder, File) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val outputFile = File(context.cacheDir, "arlo_voice_note_${System.currentTimeMillis()}.mp4")
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
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        onStart(recorder, outputFile)
    } catch (e: Exception) {
        onError("Could not initialize microphone: ${e.message}")
    }
}

private fun stopAudioRecording(recorder: MediaRecorder?) {
    try {
        recorder?.apply {
            stop()
            release()
        }
    } catch (_: Exception) {}
}
