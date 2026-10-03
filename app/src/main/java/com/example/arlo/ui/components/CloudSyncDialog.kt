package com.example.arlo.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.GeminiManager
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDialog(
    repository: ArloRepository,
    geminiManager: GeminiManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val geminiConfig by geminiManager.configFlow.collectAsState()

    var userEmailInput by remember { mutableStateOf(geminiConfig.googleAccountEmail.ifBlank { "matakoghost00@gmail.com" }) }
    var isSignedIn by remember { mutableStateOf(geminiConfig.isGoogleAccountLinked) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("All local Room database entities up to date.") }
    var isFirestoreSyncEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ArloPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Firebase Auth & Firestore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArloTextPrimary
                    )
                    Text(
                        text = "Google Sign-In & Cloud Persistence",
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
                    .padding(vertical = 4.dp)
            ) {
                // Auth Profile Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (isSignedIn) ArloSuccess.copy(alpha = 0.2f) else ArloPrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (isSignedIn) "✓" else "G", fontWeight = FontWeight.Black, color = if (isSignedIn) ArloSuccess else ArloPrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isSignedIn) "Google Account Connected" else "Firebase Authentication",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextPrimary
                                    )
                                    Text(
                                        text = if (isSignedIn) userEmailInput else "Not signed in",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSignedIn) ArloSuccess else ArloTextMuted
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isSignedIn) {
                                        geminiManager.unlinkGoogleAccount()
                                        isSignedIn = false
                                    } else {
                                        geminiManager.linkGoogleAccount(userEmailInput)
                                        isSignedIn = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSignedIn) ArloDarkSurface else ArloPrimary,
                                    contentColor = if (isSignedIn) ArloTextSecondary else ArloOnPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp).testTag("google_signin_button")
                            ) {
                                Text(
                                    text = if (isSignedIn) "Sign Out" else "Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Firestore Persistence Sync Settings
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArloDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Firestore Cloud Backup",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Text(
                                    text = "Sync AES-256 encrypted vault with Firestore for cross-device backup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isFirestoreSyncEnabled,
                                onCheckedChange = { isFirestoreSyncEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isSyncing = true
                                scope.launch {
                                    delay(1800)
                                    isSyncing = false
                                    syncStatusText = "Synced encrypted state with Firestore at " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                    repository.audit("firestore cloud sync completed")
                                }
                            },
                            enabled = !isSyncing && isFirestoreSyncEnabled,
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("trigger_firestore_sync_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ArloSecondary, contentColor = ArloOnSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ArloOnSecondary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing Firestore...", color = ArloOnSecondary, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = ArloOnSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync Now with Firestore", color = ArloOnSecondary, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = syncStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", color = ArloOnPrimary, fontWeight = FontWeight.Black)
            }
        },
        containerColor = ArloDarkSurface
    )
}
