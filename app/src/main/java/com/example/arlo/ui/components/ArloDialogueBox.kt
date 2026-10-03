package com.example.arlo.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.R
import com.example.arlo.data.AmbientAtmosphere
import com.example.arlo.data.ArloInsightBreakdown
import com.example.arlo.data.GeminiConfig
import com.example.arlo.data.GeminiManager
import com.example.arlo.model.ArloState
import com.example.arlo.model.Goal
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "Arlo" or "You"
    val text: String,
    val timestamp: String = "Just now",
    val isGeminiPowered: Boolean = true
)

@Composable
fun ArloDialogueBox(
    state: ArloState,
    breakdown: ArloInsightBreakdown,
    geminiManager: GeminiManager,
    atmosphere: AmbientAtmosphere? = null,
    onAdvanceGoal: (Goal) -> Unit,
    onCompleteMilestone: (goalId: String, milestoneId: String) -> Unit,
    onOpenGeminiSettings: () -> Unit,
    onOpenWeatherSettings: () -> Unit = {},
    onOpenConnectedApps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val geminiConfig by geminiManager.configFlow.collectAsState()

    var userMessageInput by remember { mutableStateOf("") }
    var isArloTyping by remember { mutableStateOf(false) }
    var showEmojiDrawer by remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    sender = "Arlo",
                    text = "${breakdown.greeting}\n\nHere is your breakdown from your encrypted vault. Based on where you are at with what you are moving toward:\n\n${breakdown.nextStepSuggestion}",
                    isGeminiPowered = false
                )
            )
        )
    }

    val quickActionPills = listOf(
        "🔍 Hidden Habits",
        "⚠️ Pay Attention",
        "🌟 Things Learned",
        "🎯 Next Step Moving Toward",
        "😼 Cat Motivation",
        "⚙️ Gemini Version"
    )

    val quickEmojis = listOf("🐾", "🐱", "✨", "🌸", "🎯", "🔥", "⚡", "💎", "🍃", "🛋️")

    fun sendMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || isArloTyping) return

        if (trimmed.contains("Gemini Version") || trimmed.contains("Gemini Settings")) {
            onOpenGeminiSettings()
            return
        }

        val userMsg = ChatMessage(sender = "You", text = trimmed)
        messages = messages + userMsg
        userMessageInput = ""
        isArloTyping = true

        coroutineScope.launch {
            // Scroll to newest message
            delay(100)
            chatListState.animateScrollToItem(messages.size)

            // Query Google's Gemini using the user's selected tier
            val (reply, isGemini) = geminiManager.queryGemini(trimmed, state)
            isArloTyping = false
            messages = messages + ChatMessage(
                sender = "Arlo",
                text = reply,
                isGeminiPowered = isGemini
            )
            delay(100)
            chatListState.animateScrollToItem(messages.size)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("messenger_arlo_dialogue_hub"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 1. Messenger / Snapchat Story Header Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Story 1: Arlo Cat Active Story with glowing gradient ring
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                sendMessage("Give me today's feline motivation and check my rhythm!")
                            }
                            .testTag("arlo_story_bubble")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(
                                    2.5.dp,
                                    Brush.sweepGradient(
                                        listOf(ArloPrimary, Color(0xFF8CE0FF), ArloWarmGold, ArloPrimary)
                                    ),
                                    CircleShape
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(ArloPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cat_outline),
                                    contentDescription = "Arlo Story",
                                    tint = ArloPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            // Active now dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ArloSuccess)
                                    .border(1.5.dp, ArloDarkSurface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                            // Weather & Time indicator badge
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(ArloDarkSurfaceVariant)
                                    .border(1.dp, atmosphere?.timeOfDay?.themeAccent ?: ArloPrimary, CircleShape)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(atmosphere?.weather?.icon ?: "🌤️", fontSize = 9.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arlo ${atmosphere?.weather?.icon ?: "🟢"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Story 2: Curated Top Resources (Smart discovery for current step)
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            sendMessage("What are the top 3 resources and hidden gems that will help me with my current goal phase?")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262338))
                                .border(2.dp, ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💡", fontSize = 16.sp)
                                Text("Top 3", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = ArloPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Resources ✦",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Story 3: Local Weather & Time Story
                item {
                    val weatherIcon = atmosphere?.weather?.icon ?: "☀️"
                    val timeIcon = atmosphere?.timeOfDay?.icon ?: "🌅"
                    val temp = "${atmosphere?.temperatureF ?: 70}°F"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenWeatherSettings() }
                            .testTag("weather_story_bubble")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ArloDarkSurfaceVariant)
                                .border(1.5.dp, atmosphere?.timeOfDay?.themeAccent ?: ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$timeIcon $weatherIcon", fontSize = 13.sp)
                                Text(temp, fontSize = 10.sp, color = ArloTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = atmosphere?.timeOfDay?.title?.substringBefore(" ") ?: "Weather",
                            style = MaterialTheme.typography.labelSmall,
                            color = atmosphere?.timeOfDay?.themeAccent ?: ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Story 4: Connected Apps Story
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenConnectedApps() }
                            .testTag("connected_apps_story_bubble")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ArloDarkSurfaceVariant)
                                .border(1.5.dp, ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔗", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sources 🔒",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Story 5: Gemini Model Tier Badge Story
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenGeminiSettings() }
                            .testTag("gemini_tier_story_chip")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ArloDarkSurfaceVariant)
                                .border(2.dp, ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(geminiConfig.activeTier.badgeIcon, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = geminiConfig.activeTier.displayName.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Story 6: Latest Reflection Snap
                item {
                    val latestMood = state.notes.firstOrNull()?.mood ?: "Calm"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            sendMessage("Break down my recent reflections and mindset.")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ArloDarkSurfaceVariant)
                                .border(1.5.dp, ArloBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌸", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestMood,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // 2. "What I'm Moving Toward" Hero Banner (Natural, non-intimidating phrase)
            if (breakdown.timeSensitiveGoal != null) {
                val goal = breakdown.timeSensitiveGoal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ArloDarkSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NEXT STEP YOU'RE MOVING TOWARD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloWarmGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ArloPrimaryContainer
                            ) {
                                Text(
                                    text = "${goal.progressPercent}% along",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = breakdown.nextStepSuggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action button inside hero card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (breakdown.suggestedMilestone != null) {
                                Button(
                                    onClick = {
                                        onCompleteMilestone(goal.id, breakdown.suggestedMilestone.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ArloPrimary,
                                        contentColor = ArloOnPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("complete_suggested_milestone_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mark This Step Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onAdvanceGoal(goal) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ArloPrimary,
                                        contentColor = ArloOnPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("advance_suggested_goal_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+1 Step Closer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. Messenger-Style Chat Stream
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isArlo = msg.sender == "Arlo"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isArlo) Arrangement.Start else Arrangement.End,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (isArlo) {
                                Surface(
                                    shape = CircleShape,
                                    color = ArloPrimaryContainer,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🐱", fontSize = 13.sp)
                                    }
                                }
                            }

                            // Asymmetrical Messenger bubble
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isArlo) 3.dp else 18.dp,
                                    bottomEnd = if (isArlo) 18.dp else 3.dp
                                ),
                                color = if (isArlo) ArloDarkSurfaceVariant else ArloPrimary,
                                border = if (isArlo) androidx.compose.foundation.BorderStroke(1.dp, ArloBorder) else null,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isArlo) "Arlo (Gemini ${geminiConfig.activeTier.badgeIcon})" else "You",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isArlo) ArloPrimary else Color(0xFF2A1C40),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = msg.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isArlo) ArloTextMuted else Color(0xFF3E2D58),
                                            fontSize = 9.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isArlo) ArloTextPrimary else Color.White,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Typing Indicator when Gemini is responding
                    if (isArloTyping) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = ArloPrimaryContainer,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🐱", fontSize = 13.sp)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = ArloDarkSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Arlo is pondering with Gemini ${geminiConfig.activeTier.badgeIcon}...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArloTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Quick Action Pill Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickActionPills) { pill ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ArloDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                        modifier = Modifier.clickable { sendMessage(pill) }
                    ) {
                        Text(
                            text = pill,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Emoji Reaction Drawer
            AnimatedVisibility(visible = showEmojiDrawer) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    items(quickEmojis) { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = ArloDarkSurfaceVariant,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    userMessageInput += emoji
                                    showEmojiDrawer = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Messenger / Snapchat Pill-Shaped Bottom Composer
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = ArloDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji / Reaction toggle button
                    IconButton(
                        onClick = { showEmojiDrawer = !showEmojiDrawer },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("🐾", fontSize = 18.sp)
                    }

                    // Input Field
                    OutlinedTextField(
                        value = userMessageInput,
                        onValueChange = { userMessageInput = it },
                        placeholder = {
                            Text(
                                "Message Arlo (${geminiConfig.activeTier.displayName})...",
                                fontSize = 12.sp,
                                color = ArloTextMuted
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("arlo_messenger_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(userMessageInput) })
                    )

                    // Gemini Model Indicator Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArloPrimaryContainer,
                        modifier = Modifier
                            .clickable { onOpenGeminiSettings() }
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = geminiConfig.activeTier.badgeIcon,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Send Button
                    IconButton(
                        onClick = { sendMessage(userMessageInput) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(ArloPrimary, CircleShape)
                            .testTag("arlo_messenger_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = ArloOnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
