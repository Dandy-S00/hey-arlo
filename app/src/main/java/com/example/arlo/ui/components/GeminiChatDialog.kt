package com.example.arlo.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.ChatMessage
import com.example.arlo.data.ChatRoleConfig
import com.example.arlo.data.GeminiManager
import com.example.arlo.model.ArloState
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GeminiChatDialog(
    geminiManager: GeminiManager,
    state: ArloState,
    onDismiss: () -> Unit,
    onOpenVoiceLive: () -> Unit,
    onOpenAudioTranscribe: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedRole by remember { mutableStateOf(geminiManager.availableRoles[0]) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var searchGroundingEnabled by remember { mutableStateOf(true) }
    var mapsGroundingEnabled by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "model",
                text = "Hello! I'm Arlo, your reflective companion. How is your focus and energy flowing right now?",
                modelUsed = selectedModel
            )
        )
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isBlank() || isGenerating) return

        val userMsg = ChatMessage(role = "user", text = text)
        messages.add(userMsg)
        inputText = ""
        isGenerating = true

        scope.launch {
            try {
                val res = geminiManager.sendMultiTurnChat(
                    history = messages,
                    userPrompt = text,
                    roleConfig = selectedRole,
                    modelName = selectedModel,
                    enableSearchGrounding = searchGroundingEnabled,
                    enableMapsGrounding = mapsGroundingEnabled,
                    state = state
                )
                res.onSuccess { reply ->
                    messages.add(reply)
                }.onFailure { err ->
                    messages.add(
                        ChatMessage(
                            role = "model",
                            text = "I encountered an issue: ${err.message ?: "Unable to connect"}. Let's reflect locally.",
                            modelUsed = "error"
                        )
                    )
                }
            } finally {
                isGenerating = false
                listState.animateScrollToItem(messages.size - 1)
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
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = ArloDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                            Text(selectedRole.icon, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedRole.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = selectedRole.tagline,
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onOpenVoiceLive) {
                            Text("✨🎙️", fontSize = 18.sp)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Role Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(geminiManager.availableRoles) { role ->
                        val isSelected = selectedRole.id == role.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ArloPrimary else ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedRole = role }
                                .testTag("chat_role_${role.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(role.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = role.name.take(16),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) ArloOnPrimary else ArloTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Model & Grounding Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArloDarkSurface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Pair("gemini-3.5-flash", "3.5 Flash"),
                            Pair("gemini-3.1-pro-preview", "3.1 Pro"),
                            Pair("gemini-3.1-flash-lite-preview", "Flash Lite")
                        ).forEach { (mId, mLabel) ->
                            val isSel = selectedModel == mId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ArloSecondary else Color.Transparent,
                                modifier = Modifier.clickable { selectedModel = mId }
                            ) {
                                Text(
                                    text = mLabel,
                                    color = if (isSel) ArloOnSecondary else ArloTextMuted,
                                    fontWeight = if (isSel) FontWeight.Black else FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Grounding Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = searchGroundingEnabled,
                            onClick = { searchGroundingEnabled = !searchGroundingEnabled },
                            label = { Text("Search", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Text("🌐", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary
                            )
                        )
                        FilterChip(
                            selected = mapsGroundingEnabled,
                            onClick = { mapsGroundingEnabled = !mapsGroundingEnabled },
                            label = { Text("Maps", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Text("📍", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArloPrimaryContainer,
                                selectedLabelColor = ArloPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Chat Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(ArloPrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedRole.icon, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                color = if (isUser) ArloPrimary else ArloDarkSurfaceVariant,
                                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) ArloOnPrimary else ArloTextPrimary,
                                        fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                                        lineHeight = 20.sp
                                    )

                                    if (msg.groundingSources.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🌐 Grounded Sources:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                        msg.groundingSources.take(2).forEach { src ->
                                            Text(
                                                text = "• $src",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 34.dp, top = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ArloPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$selectedModel thinking...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenAudioTranscribe,
                        modifier = Modifier
                            .size(44.dp)
                            .background(ArloDarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = ArloPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Arlo anything...", color = ArloTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary,
                            focusedContainerColor = ArloDarkSurface,
                            unfocusedContainerColor = ArloDarkSurface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ArloPrimary, CircleShape)
                            .testTag("send_chat_message_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = ArloOnPrimary)
                    }
                }
            }
        }
    }
}
