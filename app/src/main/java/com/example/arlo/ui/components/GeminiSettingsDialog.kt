package com.example.arlo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arlo.data.GeminiConfig
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.GeminiTier
import com.example.arlo.ui.theme.*

@Composable
fun GeminiSettingsDialog(
    geminiManager: GeminiManager,
    onDismiss: () -> Unit
) {
    val config by geminiManager.configFlow.collectAsState()
    var selectedTier by remember(config) { mutableStateOf(config.activeTier) }
    var apiKeyInput by remember(config) { mutableStateOf(config.apiKey) }
    var googleEmailInput by remember(config) { mutableStateOf(config.googleAccountEmail.ifBlank { "user@gmail.com" }) }
    var isSubscriberToggle by remember(config) { mutableStateOf(config.activeTier == GeminiTier.ADVANCED) }
    var cloudConsentToggle by remember(config) { mutableStateOf(config.cloudAiConsentGranted) }
    var shareIntentionsToggle by remember(config) { mutableStateOf(config.shareIntentionsInCloudContext) }
    var shareReflectionsToggle by remember(config) { mutableStateOf(config.shareReflectionsInCloudContext) }
    var showPayloadPreview by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f)
                .testTag("gemini_settings_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✦", fontSize = 20.sp, color = ArloPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Engine & Privacy Guard",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Local Model & Google Gemini Controls",
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

                // Mandatory Outbound Data Transmission Guarantee Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTier.isCompletelyLocal) ArloSuccess.copy(alpha = 0.15f) else ArloWarmGold.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedTier.isCompletelyLocal) ArloSuccess else ArloWarmGold
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (selectedTier.isCompletelyLocal) "🔒" else "🌐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedTier.isCompletelyLocal)
                                    "100% ON-DEVICE PRIVACY GUARANTEE"
                                else
                                    "EXPLICIT OUTBOUND DATA DISCLOSURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTier.isCompletelyLocal) ArloSuccess else ArloWarmGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedTier.isCompletelyLocal)
                                "Zero data will ever leave your device. All pattern analysis, reflection summaries, and intentions are processed entirely in local memory."
                            else
                                "Destination: Google Cloud API. No device files, photos, contacts, or storage are ever sent. Only your query text and authorized context leave the device over TLS 1.3.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextPrimary,
                            lineHeight = 17.sp,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Model Tier Header
                Text(
                    text = "SELECT AI MODEL & ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // All 4 Tiers: LOCAL, FREE, ADVANCED, FLASH_LITE
                GeminiTier.entries.forEach { tier ->
                    val isSelected = selectedTier == tier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedTier = tier
                                if (tier == GeminiTier.ADVANCED) isSubscriberToggle = true
                            }
                            .testTag("gemini_tier_${tier.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ArloPrimaryContainer.copy(alpha = 0.45f) else ArloDarkSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) ArloPrimary else ArloBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tier.badgeIcon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tier.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ArloTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            tier.isCompletelyLocal -> ArloSuccess.copy(alpha = 0.2f)
                                            tier == GeminiTier.ADVANCED -> ArloWarmGold.copy(alpha = 0.2f)
                                            else -> ArloPrimaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = tier.tierType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                tier.isCompletelyLocal -> ArloSuccess
                                                tier == GeminiTier.ADVANCED -> ArloWarmGold
                                                else -> ArloPrimary
                                            },
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tier.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ArloPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If Cloud Model selected: Explicit Outbound Consent & Transmission Inspector
                if (!selectedTier.isCompletelyLocal) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "EXPLICIT CLOUD TRANSMISSION PERMISSIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Main Consent Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cloudConsentToggle = !cloudConsentToggle },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = cloudConsentToggle,
                                    onCheckedChange = { cloudConsentToggle = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ArloPrimary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Authorize Google Cloud AI Transmission",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Required to send queries to Google's Gemini server",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Granular Context Controls
                            Text(
                                text = "What context may be included in prompt?",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { shareIntentionsToggle = !shareIntentionsToggle },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = shareIntentionsToggle,
                                    onCheckedChange = { shareIntentionsToggle = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Include titles of things I'm moving toward",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextPrimary,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { shareReflectionsToggle = !shareReflectionsToggle },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = shareReflectionsToggle,
                                    onCheckedChange = { shareReflectionsToggle = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Include recent reflection moods",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextPrimary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Payload Inspector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPayloadPreview = !showPayloadPreview },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showPayloadPreview) "Hide Transmission Payload" else "🔍 Inspect Outbound JSON Payload",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArloWarmGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(if (showPayloadPreview) "▲" else "▼", color = ArloWarmGold, fontSize = 10.sp)
                            }

                            AnimatedVisibility(visible = showPayloadPreview) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0E0B14),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = """
DESTINATION:
https://generativelanguage.googleapis.com/v1beta/models/${selectedTier.modelName}:generateContent

TRANSMITTED FIELDS:
• User Message: [Your typed text]
• Intentions Context: ${if (shareIntentionsToggle) "YES (Titles only)" else "NO (Withheld)"}
• Reflections Context: ${if (shareReflectionsToggle) "YES (Mood tag only)" else "NO (Withheld)"}
• Device Contents: STRICTLY ZERO (0)
• Photos / Files / Contacts: STRICTLY ZERO (0)
                                        """.trimIndent(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Account & API Key Configuration
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "GOOGLE ACCOUNT & CREDENTIALS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = googleEmailInput,
                                onValueChange = { googleEmailInput = it },
                                label = { Text("Google Account Email", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArloPrimary,
                                    unfocusedBorderColor = ArloBorder,
                                    focusedTextColor = ArloTextPrimary,
                                    unfocusedTextColor = ArloTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                label = { Text("Gemini API Key (Optional)", fontSize = 11.sp) },
                                placeholder = { Text("AIzaSy...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArloPrimary,
                                    unfocusedBorderColor = ArloBorder,
                                    focusedTextColor = ArloTextPrimary,
                                    unfocusedTextColor = ArloTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Apply Button
                Button(
                    onClick = {
                        geminiManager.setTier(selectedTier)
                        geminiManager.setApiKey(apiKeyInput)
                        geminiManager.setCloudConsent(cloudConsentToggle)
                        geminiManager.setContextSharing(shareIntentionsToggle, shareReflectionsToggle)
                        if (googleEmailInput.isNotBlank()) {
                            geminiManager.linkGoogleAccount(googleEmailInput, isSubscriberToggle)
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_gemini_settings_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply & Save Privacy Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
