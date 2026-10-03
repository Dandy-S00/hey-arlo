package com.example.arlo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.R
import com.example.arlo.ui.theme.*

@Composable
fun DailyCheckInPromptDialog(
    onCompleteCheckIn: (moodIndex: Int, reflectionText: String, energyLabel: String) -> Unit,
    onDismiss: () -> Unit
) {
    val conversationStarters = remember {
        listOf(
            "What's one gentle thing you'd love to move toward today?",
            "What is taking up the most space in your head as you wake up?",
            "How does your energy feel right now, and what kind of pace feels right?",
            "What gave you a spark or pleasant pause yesterday?",
            "If your mind was a weather forecast today, what would it look like?"
        )
    }

    var selectedMoodIndex by remember { mutableStateOf(3) }
    var starterIndex by remember { mutableStateOf(0) }
    var reflectionInput by remember { mutableStateOf("") }

    val moodOptions = listOf(
        Triple(1, "🔋", "Low / Rest"),
        Triple(2, "🛋️", "Gentle / Slow"),
        Triple(3, "☕", "Steady / Calm"),
        Triple(4, "✨", "Good / Clear"),
        Triple(5, "🚀", "High / Focused")
    )

    val currentStarter = conversationStarters[starterIndex % conversationStarters.size]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .testTag("daily_arlo_checkin_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Cat Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ArloPrimaryContainer)
                                .border(1.5.dp, ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cat_outline),
                                contentDescription = "Arlo Cat",
                                tint = ArloPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Daily Arlo Check-in",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "A casual conversation to calibrate your rhythm",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = ArloTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feline Greeting Speech Bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🐾 “Hey there! Just waking up and stretching my paws. Before the day's whirlwind begins, let's take 30 seconds to calibrate how your internal battery is humming.”",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Energy / Battery Level Selector
                Text(
                    text = "HOW IS YOUR INTERNAL BATTERY FEELING?",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moodOptions.forEach { (index, emoji, label) ->
                        val isSelected = selectedMoodIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMoodIndex = index }
                                .padding(horizontal = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) ArloPrimary else ArloBorder
                                ),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label.substringBefore(" /"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ArloPrimary else ArloTextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Casual Conversation Starter Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CASUAL THOUGHT PROMPT",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloWarmGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            IconButton(
                                onClick = { starterIndex++ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Shuffle", tint = ArloWarmGold, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentStarter,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reflection Input Field
                OutlinedTextField(
                    value = reflectionInput,
                    onValueChange = { reflectionInput = it },
                    placeholder = {
                        Text(
                            "Share a thought, feeling, or intention for today...",
                            fontSize = 12.sp,
                            color = ArloTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp)
                        .testTag("daily_checkin_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Privacy Notice Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% encrypted & local. Feeds into Arlo's pattern intelligence.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Maybe Later", color = ArloTextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val selectedMood = moodOptions.firstOrNull { it.first == selectedMoodIndex }
                            val label = selectedMood?.third ?: "Steady"
                            onCompleteCheckIn(selectedMoodIndex, reflectionInput, label)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("complete_daily_checkin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🐾 Check In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
