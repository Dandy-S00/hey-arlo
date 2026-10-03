package com.example.arlo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.data.ConnectedAppSource
import com.example.arlo.data.ConnectedAppsManager
import com.example.arlo.ui.theme.*

@Composable
fun ConnectedAppsDialog(
    connectedAppsManager: ConnectedAppsManager,
    onDismiss: () -> Unit
) {
    val sources by connectedAppsManager.sourcesFlow.collectAsState()
    var expandedSourceId by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.88f)
                .testTag("connected_apps_dialog"),
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
                                Text("🔗", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connected Apps & Sources",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Where Arlo gathers context about you",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Guarantee Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloSuccess.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Every piece of data gathered from these apps is processed strictly on-device in your AES-256 local encrypted vault. Never sent to advertising networks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTELLIGENCE INTEGRATIONS (${sources.count { it.isConnected }}/${sources.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    TextButton(
                        onClick = {
                            isSyncing = true
                            connectedAppsManager.syncAllNow()
                            isSyncing = false
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArloPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All", color = ArloPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Sources
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sources) { source ->
                        val isExpanded = expandedSourceId == source.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSourceId = if (isExpanded) null else source.id
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (source.isConnected) ArloPrimary.copy(alpha = 0.5f) else ArloBorder
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
                                        Text(source.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = source.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = ArloTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${source.category} • ${source.lastSyncTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArloTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = source.isConnected,
                                        onCheckedChange = { connectedAppsManager.toggleSource(source.id) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        HorizontalDivider(color = ArloBorder, thickness = 0.5.dp)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "EXACT INFORMATION GATHERED BY ARLO:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloWarmGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        source.informationGathered.forEach { info ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("• ", color = ArloWarmGold, fontSize = 12.sp)
                                                Text(
                                                    text = info,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ArloTextSecondary,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "WHERE IT IS STORED:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloSuccess,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = source.privacyDestination,
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

                Spacer(modifier = Modifier.height(12.dp))

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
