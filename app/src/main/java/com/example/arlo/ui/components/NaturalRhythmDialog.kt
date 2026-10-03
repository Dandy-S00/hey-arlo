package com.example.arlo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.data.NaturalRhythmManager
import com.example.arlo.ui.theme.*

@Composable
fun NaturalRhythmDialog(
    rhythmManager: NaturalRhythmManager,
    onDismiss: () -> Unit
) {
    val profile by rhythmManager.rhythmProfileFlow.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.85f)
                .testTag("natural_rhythm_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ArloPrimaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(profile.chronotype.icon, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Natural Rhythm & Heartbeat",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Learned from your natural habits",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Chronotype Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(profile.chronotype.icon, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = profile.chronotype.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = ArloPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Learned Chronotype Profile",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ArloPrimaryContainer
                                    ) {
                                        Text(
                                            text = profile.confidenceLevel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = profile.chronotype.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    lineHeight = 17.sp,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 2. Adaptive Heartbeat & Checkup Tuning Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloWarmGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto-Tune Heartbeat & Updates",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ArloTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Syncs 32h briefings to your natural interval",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Switch(
                                        checked = profile.isAutoTuneEnabled,
                                        onCheckedChange = { rhythmManager.setAutoTuneEnabled(it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = ArloBorder, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Heartbeat Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ArloDarkSurface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${profile.adaptiveHeartbeatHours}h",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = ArloWarmGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Adaptive Heartbeat",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ArloDarkSurface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = profile.activeWindowText,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = ArloPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Active Window",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Dynamic Checkup Schedule Shift Report
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "NATURAL CHECKUP SCHEDULE SHIFTS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val shifts = listOf(
                                    Triple("🌅 Morning Kickoff", "Shifted to ${profile.learnedWakeHour}:15 AM", "Gently prompts focus intentions as you naturally wake."),
                                    Triple("⚡ Peak Deep Work", "Calibrated around ${profile.peakFocusHour}:00", "Surfaces your top 3 curated resources when focus is highest."),
                                    Triple("🌙 Evening Wind-Down", "Shifted to ${(profile.learnedSleepHour - 1 + 24) % 24}:15 PM", "Invites quiet reflection before your natural rest window.")
                                )

                                shifts.forEach { (title, time, desc) ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ArloPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("🐾", fontSize = 10.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(title, style = MaterialTheme.typography.bodySmall, color = ArloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("• $time", style = MaterialTheme.typography.labelSmall, color = ArloWarmGold, fontSize = 11.sp)
                                            }
                                            Text(desc, style = MaterialTheme.typography.labelSmall, color = ArloTextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Activity Density Breakdown
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloDarkSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🔒", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All schedule learning is calculated 100% locally on this device. Zero activity timestamps are ever transmitted to any external server.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Got It! (Sync Rhythm)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
