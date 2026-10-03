package com.example.arlo.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.ChatMessage
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.GeminiTier
import com.example.arlo.model.ArloState
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ArloCompanionScreen(
    state: ArloState,
    repository: ArloRepository,
    geminiManager: GeminiManager,
    onOpenLiveVoice: () -> Unit,
    onOpenAudioTranscribeDialog: () -> Unit,
    onOpenConnectors: () -> Unit,
    onOpenAudioRecorder: () -> Unit,
    onOpenVideoToText: () -> Unit,
    onOpenCatReminders: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedRole by remember { mutableStateOf(geminiManager.availableRoles[0]) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var searchGroundingEnabled by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Voice Dictation state
    var isVoiceRecording by remember { mutableStateOf(false) }
    var voiceRecordDurationSec by remember { mutableIntStateOf(0) }
    var isTranscribingVoice by remember { mutableStateOf(false) }
    var voiceRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var voiceAudioFile by remember { mutableStateOf<File?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "model",
                text = "🐾 *Purrs gently* Hello! I'm Arlo, your reflective feline companion.\n\nYou can chat with me here via text or tap the microphone for voice-to-text. I'm connected to your notes, heartbeat, tasks, and goals in your Life Vault. How can I support you right now?",
                modelUsed = selectedModel
            )
        )
    }

    fun handleVoiceTranscription(file: File) {
        isTranscribingVoice = true
        scope.launch {
            try {
                val res = geminiManager.transcribeAudioFile(file)
                if (res.isSuccess) {
                    val text = res.getOrNull() ?: ""
                    if (text.isNotBlank()) {
                        inputText = text
                        Toast.makeText(context, "Voice transcribed into text!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val err = res.exceptionOrNull()
                    Toast.makeText(context, "Transcription note: ${err?.message ?: "ready"}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isTranscribingVoice = false
                try { file.delete() } catch (_: Exception) {}
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val cacheDir = context.cacheDir
                val file = File(cacheDir, "arlo_voice_${System.currentTimeMillis()}.mp4")
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
                voiceRecorder = recorder
                voiceAudioFile = file
                isVoiceRecording = true
                voiceRecordDurationSec = 0
            } catch (e: Exception) {
                Toast.makeText(context, "Microphone error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice dictation", Toast.LENGTH_SHORT).show()
        }
    }

    // Voice recording timer
    LaunchedEffect(isVoiceRecording) {
        if (isVoiceRecording) {
            while (isVoiceRecording) {
                delay(1000)
                voiceRecordDurationSec++
            }
        }
    }

    fun stopVoiceRecordingAndTranscribe() {
        try {
            voiceRecorder?.stop()
            voiceRecorder?.release()
            voiceRecorder = null
            isVoiceRecording = false
            voiceAudioFile?.let { handleVoiceTranscription(it) }
        } catch (e: Exception) {
            isVoiceRecording = false
            Toast.makeText(context, "Recording stopped: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendMessage(customText: String? = null) {
        val textToSend = (customText ?: inputText).trim()
        if (textToSend.isBlank() || isGenerating) return

        val userMsg = ChatMessage(role = "user", text = textToSend)
        messages.add(userMsg)
        if (customText == null) inputText = ""
        isGenerating = true

        scope.launch {
            try {
                val res = geminiManager.sendMultiTurnChat(
                    history = messages,
                    userPrompt = textToSend,
                    roleConfig = selectedRole,
                    modelName = selectedModel,
                    enableSearchGrounding = searchGroundingEnabled,
                    enableMapsGrounding = false,
                    state = state
                )
                res.onSuccess { reply ->
                    messages.add(reply)
                }.onFailure { err ->
                    messages.add(
                        ChatMessage(
                            role = "model",
                            text = "🐾 I'm listening quietly. I ran into a connection glitch: ${err.message ?: "Network unreachable"}. Let's reflect from your local vault.",
                            modelUsed = "offline-fallback"
                        )
                    )
                }
            } finally {
                isGenerating = false
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val starterPrompts = listOf(
        "☀️ How is my focus today?",
        "📋 Review my active sprint tasks",
        "🎯 What goal should I work on next?",
        "🌿 Give me a mindful reflection",
        "🔄 Check my connected integrations"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArloDarkBackground)
    ) {
        // Subtle ambient feline paw background
        com.example.arlo.ui.components.CatBackgroundPaws()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Companion Bar
            Surface(
                color = ArloDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(ArloPrimaryContainer, CircleShape)
                                    .clickable {
                                        Toast.makeText(context, "Prrrrr... Arlo is kneading biscuits for you! 🐾", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(state.avatar.ifBlank { "🐾" }, fontSize = 22.sp)
                            }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Arlo Interactive",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ArloSuccess.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "HEARTBEAT OK",
                                        color = ArloSuccess,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Text or voice to chat • Connected to your Life Vault",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Live Voice Mode Action
                    Button(
                        onClick = onOpenLiveVoice,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimaryContainer,
                            contentColor = ArloPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("companion_live_voice_button")
                    ) {
                        Text("🎙️ Live Voice", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Model & Search Grounding Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(GeminiTier.values()) { tier ->
                            val isSelected = selectedModel == tier.modelName
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                                modifier = Modifier.clickable { selectedModel = tier.modelName }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tier.badgeIcon, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tier.displayName.take(15),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloOnPrimary else ArloTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (searchGroundingEnabled) ArloPrimary.copy(alpha = 0.2f) else ArloDarkSurfaceVariant,
                        modifier = Modifier.clickable { searchGroundingEnabled = !searchGroundingEnabled }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (searchGroundingEnabled) "Search ON" else "Search OFF",
                                color = if (searchGroundingEnabled) ArloPrimary else ArloTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(ArloPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐾", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) ArloPrimary else ArloDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isUser) ArloPrimary else ArloBorder
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) ArloOnPrimary else ArloTextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                if (msg.groundingSources.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sources:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloPrimary,
                                        fontSize = 10.sp
                                    )
                                    msg.groundingSources.take(2).forEach { src ->
                                        Text(
                                            text = "• ${src.take(35)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                if (!isUser && msg.text.length > 30) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ArloDarkSurface,
                                            modifier = Modifier.clickable {
                                                repository.addTask(msg.text.take(80))
                                                Toast.makeText(context, "Saved to tasks!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text(
                                                text = "+ Task",
                                                color = ArloPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ArloDarkSurface,
                                            modifier = Modifier.clickable {
                                                repository.addNote(body = msg.text, mood = "Reflective", prompt = "Saved from Arlo Chat")
                                                Toast.makeText(context, "Saved to notes!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text(
                                                text = "+ Note",
                                                color = ArloWarmGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(ArloPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐾", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ArloDarkSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = ArloPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Arlo is reflecting...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Starter & Media Prompts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary),
                    modifier = Modifier.clickable { onOpenAudioRecorder() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎙️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Record Call / Live Audio",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary),
                    modifier = Modifier.clickable { onOpenVideoToText() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📹", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Video Link to Notes",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary),
                    modifier = Modifier.clickable { onOpenCatReminders() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔔", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cat Reminders",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(starterPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                    modifier = Modifier.clickable { sendMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Active Voice Recording Bar (When mic is active)
        AnimatedVisibility(visible = isVoiceRecording || isTranscribingVoice) {
            Surface(
                color = ArloPrimaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .scale(if (isVoiceRecording) pulseScale else 1f)
                                .background(ArloDanger, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isVoiceRecording) "Listening... 0:${voiceRecordDurationSec.toString().padStart(2, '0')}" else "Transcribing speech to text...",
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (isVoiceRecording) {
                        Button(
                            onClick = { stopVoiceRecordingAndTranscribe() },
                            colors = ButtonDefaults.buttonColors(containerColor = ArloDanger, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ArloPrimary, strokeWidth = 2.dp)
                    }
                }
            }
        }

        // Bottom Text & Voice Input Bar
        Surface(
            color = ArloDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1-Tap Microphone Voice-to-Text Button
                IconButton(
                    onClick = {
                        if (isVoiceRecording) {
                            stopVoiceRecordingAndTranscribe()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isVoiceRecording) ArloDanger.copy(alpha = 0.2f) else ArloPrimaryContainer,
                            CircleShape
                        )
                        .testTag("voice_to_text_mic_button")
                ) {
                    Icon(
                        imageVector = if (isVoiceRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice to text",
                        tint = if (isVoiceRecording) ArloDanger else ArloPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Text Arlo or tap mic to speak...", color = ArloTextMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("companion_chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary,
                        focusedContainerColor = ArloDarkBackground,
                        unfocusedContainerColor = ArloDarkBackground
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = { sendMessage() },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (inputText.isNotBlank()) ArloPrimary else ArloDarkSurfaceVariant,
                            CircleShape
                        )
                        .testTag("companion_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) ArloOnPrimary else ArloTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
}
