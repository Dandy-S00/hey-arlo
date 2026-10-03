package com.example.arlo.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.ArloRepository
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.GeminiTier
import com.example.arlo.model.ArloState
import com.example.arlo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    state: ArloState,
    repository: ArloRepository,
    geminiManager: GeminiManager,
    onLockArlo: () -> Unit,
    onResetVault: () -> Unit,
    onOpenConnectors: () -> Unit,
    onOpenCloudSync: () -> Unit
) {
    val context = LocalContext.current
    val geminiConfig by geminiManager.configFlow.collectAsState()

    var userName by remember { mutableStateOf("Human Companion") }
    var selectedAvatar by remember { mutableStateOf(state.avatar.ifBlank { "🐱" }) }
    var selectedTone by remember { mutableStateOf("Gentle & Wise") }
    var purrHapticsEnabled by remember { mutableStateOf(true) }
    var catQuipsEnabled by remember { mutableStateOf(true) }

    // Passphrase Change state
    var showPassphraseDialog by remember { mutableStateOf(false) }
    var oldPassphrase by remember { mutableStateOf("") }
    var newPassphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }

    // Reset Confirmation dialog
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val catAvatars = listOf(
        "🐱" to "Calico Arlo",
        "🐾" to "Tuxedo Arlo",
        "🐈" to "Scottish Fold",
        "🐆" to "Velvet Panther",
        "🐯" to "Orange Tabby",
        "😺" to "Happy Whiskers"
    )

    val felineTones = listOf("Gentle & Wise", "Playful & Motivating", "Quiet & Stoic", "Deep Philosopher")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArloDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Cat Companion Profile & Personalization
        item {
            Text(
                text = "Cat Companion & Personalization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArloTextPrimary
            )
            Text(
                text = "Customize Arlo's feline persona, avatar, and behavior",
                style = MaterialTheme.typography.bodySmall,
                color = ArloTextSecondary,
                fontSize = 11.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ArloDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Choose Arlo's Cat Avatar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArloPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(catAvatars) { (emoji, label) ->
                            val isSelected = selectedAvatar == emoji
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) ArloPrimary else ArloDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArloPrimary else ArloBorder
                                ),
                                modifier = Modifier.clickable {
                                    selectedAvatar = emoji
                                    repository.updateAvatar(emoji)
                                    Toast.makeText(context, "Updated avatar to $label!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ArloOnPrimary else ArloTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = ArloBorder.copy(alpha = 0.5f))

                    Text(
                        text = "Feline Tone of Voice",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArloPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(felineTones) { tone ->
                            val isSelected = selectedTone == tone
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ArloPrimaryContainer else ArloDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArloPrimary else ArloBorder
                                ),
                                modifier = Modifier.clickable { selectedTone = tone }
                            ) {
                                Text(
                                    text = tone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) ArloPrimary else ArloTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Divider(color = ArloBorder.copy(alpha = 0.5f))

                    // Cat Interaction Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Purr Haptics & Vibration", style = MaterialTheme.typography.bodyMedium, color = ArloTextPrimary, fontWeight = FontWeight.Bold)
                            Text("Gentle purr vibrations upon task completion", style = MaterialTheme.typography.bodySmall, color = ArloTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = purrHapticsEnabled,
                            onCheckedChange = { purrHapticsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary, checkedTrackColor = ArloPrimaryContainer)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cat Quips & Mindful Meows", style = MaterialTheme.typography.bodyMedium, color = ArloTextPrimary, fontWeight = FontWeight.Bold)
                            Text("Friendly feline encouragement during focus check-ins", style = MaterialTheme.typography.bodySmall, color = ArloTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = catQuipsEnabled,
                            onCheckedChange = { catQuipsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary, checkedTrackColor = ArloPrimaryContainer)
                        )
                    }
                }
            }
        }

        // Section: AI Intelligence & Gemini Models
        item {
            Text(
                text = "Google Cloud & AI Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArloTextPrimary
            )
            Text(
                text = "Configure Gemini neural models, search grounding, and privacy tiers",
                style = MaterialTheme.typography.bodySmall,
                color = ArloTextSecondary,
                fontSize = 11.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ArloDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Active Intelligence Tier",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArloPrimary
                    )

                    GeminiTier.values().forEach { tier ->
                        val isSelected = geminiConfig.activeTier == tier
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ArloPrimaryContainer else ArloDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ArloPrimary else ArloBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { geminiManager.setTier(tier) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(tier.badgeIcon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tier.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ArloPrimary else ArloTextPrimary
                                        )
                                        Text(
                                            text = tier.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArloTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = ArloPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Divider(color = ArloBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Google Search Grounding", style = MaterialTheme.typography.bodyMedium, color = ArloTextPrimary, fontWeight = FontWeight.Bold)
                            Text("Ground chat answers with real-time web citations", style = MaterialTheme.typography.bodySmall, color = ArloTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = geminiConfig.enableSearchGrounding,
                            onCheckedChange = { geminiManager.setGrounding(searchEnabled = it, mapsEnabled = geminiConfig.enableMapsGrounding) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary, checkedTrackColor = ArloPrimaryContainer)
                        )
                    }
                }
            }
        }

        // Section: Cloud Sync & Integrations
        item {
            Text(
                text = "Integrations & Cloud Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArloTextPrimary
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ArloDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenConnectors,
                        colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open All-Integrations Sync Hub", fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = onOpenCloudSync,
                        colors = ButtonDefaults.buttonColors(containerColor = ArloPrimaryContainer, contentColor = ArloPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Firebase Firestore Envelope Sync", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Security & Vault Encryption
        item {
            Text(
                text = "Security, Passphrase & Vault",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArloTextPrimary
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ArloDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showPassphraseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextPrimary)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp), tint = ArloPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Vault Passphrase")
                    }

                    OutlinedButton(
                        onClick = {
                            val backup = repository.exportDecryptedJson()
                            if (backup != null) {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Arlo Vault Backup", backup))
                                Toast.makeText(context, "Encrypted vault JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloTextPrimary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = ArloWarmGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Vault Data Backup (JSON)")
                    }

                    OutlinedButton(
                        onClick = onLockArlo,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArloDanger)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = ArloDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Vault Immediately")
                    }

                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ArloDangerContainer, contentColor = ArloDanger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset & Erase Vault Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Change Passphrase Dialog
    if (showPassphraseDialog) {
        AlertDialog(
            onDismissRequest = { showPassphraseDialog = false },
            title = { Text("Change Vault Passphrase", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassphrase,
                        onValueChange = { oldPassphrase = it },
                        label = { Text("Current Passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassphrase,
                        onValueChange = { newPassphrase = it },
                        label = { Text("New Passphrase (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassphrase,
                        onValueChange = { confirmPassphrase = it },
                        label = { Text("Confirm New Passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassphrase != confirmPassphrase) {
                            Toast.makeText(context, "New passphrases do not match!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPassphrase.length < 6) {
                            Toast.makeText(context, "Passphrase must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = repository.changePassphrase(oldPassphrase, newPassphrase)
                        if (res.isSuccess) {
                            Toast.makeText(context, "Passphrase updated successfully!", Toast.LENGTH_SHORT).show()
                            showPassphraseDialog = false
                            oldPassphrase = ""
                            newPassphrase = ""
                            confirmPassphrase = ""
                        } else {
                            Toast.makeText(context, "Failed: Current passphrase incorrect.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Update Passphrase", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPassphraseDialog = false }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }

    // Reset Vault Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Entire Vault?", fontWeight = FontWeight.Bold, color = ArloDanger) },
            text = {
                Text(
                    text = "This will permanently wipe all local encrypted notes, tasks, goals, memories, and passphrases. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArloTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetVault()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloDanger, contentColor = Color.White)
                ) {
                    Text("Erase Everything", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Keep Safe", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
