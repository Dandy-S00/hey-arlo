package com.example.arlo.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.arlo.data.GeminiManager
import com.example.arlo.model.ArloState
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Composable
fun LiveVoiceConversationDialog(
    geminiManager: GeminiManager,
    state: ArloState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLiveActive by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var liveTranscript by remember { mutableStateOf("Tap the glowing orb to start real-time Live Voice conversation with Arlo.") }
    var aiLiveResponse by remember { mutableStateOf("") }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioOutputFile by remember { mutableStateOf<File?>(null) }

    // Initialize TTS
    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (_: Exception) {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "live_orb")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    fun speakAiText(text: String) {
        aiLiveResponse = text
        isSpeaking = true
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "arlo_live_voice")
        scope.launch {
            delay((text.length * 70L).coerceIn(2000L, 8000L))
            isSpeaking = false
        }
    }

    fun stopRecordingAndQuery() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isListening = false

            val file = audioOutputFile
            if (file != null && file.exists()) {
                isThinking = true
                liveTranscript = "Processing live audio input..."
                scope.launch {
                    try {
                        val audioBytes = file.readBytes()
                        val transRes = geminiManager.transcribeAudio(audioBytes)
                        transRes.onSuccess { transcribed ->
                            liveTranscript = "\"$transcribed\""
                            val reply = geminiManager.generateResponse(
                                "You are Arlo speaking in real-time Live Voice conversation (model: gemini-2.5-flash-native-audio-preview-12-2025). " +
                                        "Keep your answer spoken, concise (1-2 sentences), warm, conversational, and direct. " +
                                        "User just said: $transcribed"
                            )
                            val finalReply = if (reply.isNotBlank()) reply else "I hear you clearly. Let's take a calm breath and tackle this together."
                            speakAiText(finalReply)
                        }.onFailure {
                            liveTranscript = "Could not parse audio clearly. Tap again to speak."
                        }
                    } catch (e: Exception) {
                        liveTranscript = "Connection interrupted. Let's try again."
                    } finally {
                        isThinking = false
                    }
                }
            }
        } catch (_: Exception) {
            isListening = false
            isThinking = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLiveActive = true
            isListening = true
            try {
                val file = File(context.cacheDir, "arlo_live_${System.currentTimeMillis()}.mp4")
                audioOutputFile = file
                val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                liveTranscript = "Listening... Speak naturally."
            } catch (e: Exception) {
                liveTranscript = "Microphone error: ${e.message}"
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
            shape = RoundedCornerShape(28.dp),
            color = ArloDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
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
                                .size(36.dp)
                                .background(ArloPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gemini Live API",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "gemini-2.5-flash-native-audio",
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

                // Central Glowing Orb Visualizer
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(if (isLiveActive) orbPulse else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2.4f

                        // Multi-layer ambient aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isSpeaking) ArloWarmGold.copy(alpha = 0.5f)
                                    else if (isListening) ArloPrimary.copy(alpha = 0.6f)
                                    else ArloSecondary.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = radius * 1.4f
                            ),
                            radius = radius * 1.4f,
                            center = center
                        )

                        // Core Orb
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    if (isSpeaking) Color(0xFFF6D365) else Color(0xFFD8B4E2),
                                    if (isSpeaking) Color(0xFFFDA085) else Color(0xFF8E9AAF)
                                )
                            ),
                            radius = radius,
                            center = center
                        )
                    }

                    // Center Icon / Status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (isListening) {
                                stopRecordingAndQuery()
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    isLiveActive = true
                                    isListening = true
                                    try {
                                        val file = File(context.cacheDir, "arlo_live_${System.currentTimeMillis()}.mp4")
                                        audioOutputFile = file
                                        val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                            MediaRecorder(context)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            MediaRecorder()
                                        }.apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                            setOutputFile(file.absolutePath)
                                            prepare()
                                            start()
                                        }
                                        mediaRecorder = recorder
                                        liveTranscript = "Listening... Speak freely."
                                    } catch (e: Exception) {
                                        liveTranscript = "Mic error: ${e.message}"
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = if (isSpeaking) "🗣️" else if (isListening) "🎙️" else "✦",
                            fontSize = 38.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeaking) "Arlo Speaking"
                            else if (isThinking) "Thinking..."
                            else if (isListening) "Tap to Finish"
                            else "Tap to Speak",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1E0E2E),
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Live Conversation Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Live Voice Context",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = liveTranscript,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Start
                            )

                            if (aiLiveResponse.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Arlo's Voice:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloWarmGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = aiLiveResponse,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ArloTextPrimary,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                ttsEngine?.stop()
                                isSpeaking = false
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(ArloDarkSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Mute TTS", tint = ArloTextSecondary)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                if (isListening) {
                                    stopRecordingAndQuery()
                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        isListening = true
                                        isLiveActive = true
                                        try {
                                            val file = File(context.cacheDir, "arlo_live_${System.currentTimeMillis()}.mp4")
                                            audioOutputFile = file
                                            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                                MediaRecorder(context)
                                            } else {
                                                @Suppress("DEPRECATION")
                                                MediaRecorder()
                                            }.apply {
                                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                                setOutputFile(file.absolutePath)
                                                prepare()
                                                start()
                                            }
                                            mediaRecorder = recorder
                                            liveTranscript = "Listening..."
                                        } catch (e: Exception) {
                                            liveTranscript = "Mic error: ${e.message}"
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) ArloDanger else ArloPrimary,
                                contentColor = if (isListening) Color.White else ArloOnPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(48.dp).testTag("toggle_live_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isListening) Color.White else ArloOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListening) "End Speech" else "Speak to Arlo",
                                color = if (isListening) Color.White else ArloOnPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
