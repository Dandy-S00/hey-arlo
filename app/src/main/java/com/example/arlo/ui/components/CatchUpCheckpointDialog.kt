package com.example.arlo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.arlo.data.CadenceReminderManager
import com.example.arlo.model.CatchUpBriefing
import com.example.arlo.model.GamificationState
import com.example.arlo.ui.theme.*

@Composable
fun CatchUpCheckpointDialog(
    briefing: CatchUpBriefing,
    cadenceManager: CadenceReminderManager,
    onDismiss: () -> Unit
) {
    val gamification by cadenceManager.gamificationFlow.collectAsState()

    val availableAccessories = listOf(
        Pair("🧣 Cozy Knit Scarf", 0),
        Pair("🎀 Red Ribbon Collar", 0),
        Pair("🕶️ Cool Sunglasses", 4),
        Pair("🎩 Wizard Top Hat", 8),
        Pair("👑 Golden Crown", 12),
        Pair("🪐 Galaxy Star Cape", 15)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.88f)
                .testTag("catch_up_checkpoint_dialog"),
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
                                Text("⏱️", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "32-Hour Checkpoint",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Catch up, realign, and take gentle steps",
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
                    // 1. Falling Behind Diagnostic Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloWarmGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🐾", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WHERE THINGS CURRENTLY STAND",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloWarmGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                            Text("${briefing.overdueTasksCount}", style = MaterialTheme.typography.titleLarge, color = ArloPrimary, fontWeight = FontWeight.Bold)
                                            Text("Pending Tasks", style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 10.sp)
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
                                            Text("${briefing.pendingMilestonesCount}", style = MaterialTheme.typography.titleLarge, color = ArloWarmGold, fontWeight = FontWeight.Bold)
                                            Text("Stalled Milestones", style = MaterialTheme.typography.labelSmall, color = ArloTextMuted, fontSize = 10.sp)
                                        }
                                    }
                                }

                                if (briefing.stalledGoals.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Areas needing a gentle nudge: ${briefing.stalledGoals.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Recovery Steps to Get Back on Track
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "3 STEPS TO GET BACK ON TRACK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                briefing.recoverySteps.forEachIndexed { index, step ->
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
                                                Text("${index + 1}", fontSize = 11.sp, color = ArloPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArloTextPrimary,
                                            lineHeight = 17.sp,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Offer to Gamify Experience
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (gamification.isEnabled) Color(0xFF1E2838) else ArloDarkSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (gamification.isEnabled) ArloPrimary else ArloBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🎮", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Gamify the Experience?",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Turn task recovery into a rewarding Cat Quest RPG",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = gamification.isEnabled,
                                        onCheckedChange = { cadenceManager.setGamificationEnabled(it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                                    )
                                }

                                AnimatedVisibility(visible = gamification.isEnabled) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        HorizontalDivider(color = ArloBorder, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Player Status Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Level ${gamification.level} • ${gamification.currentRank}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = ArloPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "XP: ${gamification.currentXp} / ${gamification.xpForNextLevel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ArloTextMuted,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = ArloDarkSurface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🐟", fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${gamification.tunaTreats} Treats",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ArloWarmGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { gamification.progressFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = ArloPrimary,
                                            trackColor = ArloDarkSurface
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "CAT ACCESSORIES WARDROBE (Equipped: ${gamification.equippedAccessory}):",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(availableAccessories) { (acc, cost) ->
                                                val isUnlocked = gamification.unlockedAccessories.contains(acc)
                                                val isEquipped = gamification.equippedAccessory == acc
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isEquipped) ArloPrimaryContainer else ArloDarkSurface,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isEquipped) ArloPrimary else ArloBorder
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        if (isUnlocked) {
                                                            cadenceManager.equipAccessory(acc)
                                                        } else if (gamification.tunaTreats >= cost) {
                                                            cadenceManager.unlockAccessory(acc, cost)
                                                        }
                                                    }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(acc, fontSize = 11.sp, color = ArloTextPrimary)
                                                        if (!isUnlocked) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("($cost 🐟)", fontSize = 10.sp, color = ArloWarmGold)
                                                        } else if (isEquipped) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = ArloPrimary, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Cat Mindset Wisdom Quote
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ArloDarkSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("💬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = briefing.motivationalQuote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            cadenceManager.dismissCheckpoint()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Snooze 32h", color = ArloTextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            cadenceManager.dismissCheckpoint()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloPrimary,
                            contentColor = ArloOnPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("I'm Back on Track!", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
