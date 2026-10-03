package com.example.arlo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.ui.theme.*

@Composable
fun VaultScreen(
    hasExistingVault: Boolean,
    hasLegacyData: Boolean,
    isBiometricAvailable: Boolean = false,
    isBiometricEnabled: Boolean = false,
    onTriggerBiometric: () -> Unit = {},
    onUnlock: (passphrase: String, enableBiometrics: Boolean) -> Result<Unit>,
    onCreate: (passphrase: String, migrateLegacy: Boolean, enableBiometrics: Boolean) -> Result<Unit>,
    onResetVault: () -> Unit
) {
    var mode by remember(hasExistingVault) {
        mutableStateOf(if (hasExistingVault) "unlock" else "create")
    }
    var passphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var migrateLegacy by remember { mutableStateOf(hasLegacyData) }
    var enableBiometrics by remember { mutableStateOf(isBiometricAvailable) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Auto-trigger biometric on launch if enabled
    LaunchedEffect(isBiometricEnabled, mode) {
        if (isBiometricEnabled && mode == "unlock") {
            onTriggerBiometric()
        }
    }

    fun submit() {
        errorMessage = null
        if (mode == "unlock") {
            if (passphrase.isBlank()) {
                errorMessage = "Please enter your passphrase."
                return
            }
            val result = onUnlock(passphrase, enableBiometrics)
            result.onFailure {
                errorMessage = it.message ?: "Unable to open the vault. Check the passphrase and try again."
            }
        } else {
            if (passphrase.length < 12) {
                errorMessage = "Passphrase must be at least 12 characters."
                return
            }
            if (passphrase != confirmPassphrase) {
                errorMessage = "The passphrases do not match."
                return
            }
            val result = onCreate(passphrase, migrateLegacy, enableBiometrics)
            result.onFailure {
                errorMessage = it.message ?: "Failed to create vault."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArloDarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ArloPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦",
                            color = ArloPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Arlo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArloTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ArloDarkSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "PRIVATE LOCAL VAULT • ROOM DB",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = if (mode == "unlock") "Unlock Arlo" else "Protect your Arlo data",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ArloTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (mode == "unlock")
                        "Enter your passphrase or use biometric unlock to open your encrypted local vault."
                    else
                        "Your data stays encrypted with AES-256 on this device. Create a strong passphrase.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArloTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Biometric Unlock Button (If in unlock mode and biometrics supported)
                if (mode == "unlock" && isBiometricAvailable) {
                    Button(
                        onClick = onTriggerBiometric,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("biometric_unlock_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloSecondary,
                            contentColor = ArloOnSecondary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Unlock",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unlock with Fingerprint / Face",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = ArloBorder)
                        Text(
                            text = "  or enter passphrase  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloTextMuted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = ArloBorder)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArloDangerContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ArloDanger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Passphrase Input
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Vault passphrase", fontWeight = FontWeight.SemiBold) },
                    placeholder = { Text(if (mode == "unlock") "Enter passphrase" else "Min 12 characters") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_passphrase_input"),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = ArloTextMuted
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (mode == "unlock") ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (mode == "unlock") submit() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary,
                        focusedLabelColor = ArloPrimary,
                        unfocusedLabelColor = ArloTextMuted
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm input in create mode
                if (mode == "create") {
                    OutlinedTextField(
                        value = confirmPassphrase,
                        onValueChange = { confirmPassphrase = it },
                        label = { Text("Confirm passphrase", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("Re-enter passphrase") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_confirm_input"),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submit() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArloPrimary,
                            unfocusedBorderColor = ArloBorder,
                            focusedTextColor = ArloTextPrimary,
                            unfocusedTextColor = ArloTextPrimary,
                            focusedLabelColor = ArloPrimary,
                            unfocusedLabelColor = ArloTextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isBiometricAvailable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = enableBiometrics,
                                onCheckedChange = { enableBiometrics = it },
                                colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enable 1-tap Biometric Unlock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ArloTextPrimary
                            )
                        }
                    }

                    if (hasLegacyData) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = migrateLegacy,
                                onCheckedChange = { migrateLegacy = it },
                                colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import my existing local prototype data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextSecondary
                            )
                        }
                    }
                } else if (isBiometricAvailable && !isBiometricEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = enableBiometrics,
                            onCheckedChange = { enableBiometrics = it },
                            colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save for Biometric Unlock (Fingerprint / Face)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ArloTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Submit Button (Pastel background + Heavy bold high-contrast text)
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("vault_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ArloOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mode == "unlock") "Unlock securely" else "Create encrypted vault",
                        color = ArloOnPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                if (mode == "create") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val generatedPassphrase = "arlo-vault-" + java.util.UUID.randomUUID().toString().take(16)
                            val result = onCreate(generatedPassphrase, migrateLegacy, enableBiometrics)
                            result.onFailure {
                                errorMessage = it.message ?: "Failed to generate quick start vault."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("vault_quick_start_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, ArloPrimary.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "✦ 1-Tap Quick Start (Instant Setup)",
                            color = ArloPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode toggle or Reset option
                if (hasExistingVault) {
                    if (mode == "unlock") {
                        TextButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.testTag("vault_reset_button")
                        ) {
                            Text("Reset vault / start fresh", color = ArloDanger, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { mode = "unlock" }) {
                            Text("Back to unlock", color = ArloPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Room SQLite & AES-256-GCM local storage. Zero data sent to any server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArloTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Encrypted Vault?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete your local encrypted Room database permanently. Any stored goals, tasks, reflections, and notes will be cleared.",
                    color = ArloTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetVault()
                        mode = "create"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloDanger)
                ) {
                    Text("Delete and Start Fresh", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = ArloTextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
