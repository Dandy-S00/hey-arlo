package com.example.arlo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.R
import com.example.arlo.data.FloatingBubblePreferenceManager
import com.example.arlo.ui.theme.*

@Composable
fun FloatingBubbleSettingsDialog(
    floatingBubbleManager: FloatingBubblePreferenceManager,
    onDismiss: () -> Unit
) {
    val isEnabled by floatingBubbleManager.isBubbleEnabled.collectAsState()
    val hasPermission = floatingBubbleManager.hasOverlayPermission()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .testTag("floating_bubble_settings_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                Text("💬", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Floating Chat Bubble",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Chat with Arlo even when app is closed",
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

                // Interactive Graphic Demonstration
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ArloPrimaryContainer)
                                .border(2.dp, ArloPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cat_outline),
                                contentDescription = "Arlo Bubble",
                                tint = ArloPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Always-Accessible Cat Chat Head",
                            style = MaterialTheme.typography.titleSmall,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Floats over any other app, social feeds, and home screen. Tap the bubble to chat with Arlo, check intentions, or log reflections instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Floating Chat Head",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnabled) "Active: Floating bubble is running" else "Disabled: Only visible inside app",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) ArloSuccess else ArloTextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { floatingBubbleManager.setBubbleEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                        )
                    }
                }

                // Overlay Permission Guidance if needed
                if (!hasPermission) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArloWarmGold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloWarmGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Android System Permission Required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloWarmGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To float over other apps when Arlo is closed, please grant the 'Display over other apps' permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArloTextPrimary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { floatingBubbleManager.requestOverlayPermission() },
                                colors = ButtonDefaults.buttonColors(containerColor = ArloWarmGold, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Grant 'Display over other apps'", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
