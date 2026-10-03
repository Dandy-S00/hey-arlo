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
    onUnlock: (String) -> Result<Unit>,
    onCreate: (String, Boolean) -> Result<Unit>,
    onResetVault: () -> Unit
) {
    var mode by remember(hasExistingVault) {
        mutableStateOf(if (hasExistingVault) "unlock" else "create")
    }
    var passphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var migrateLegacy by remember { mutableStateOf(hasLegacyData) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    fun submit() {
        errorMessage = null
        if (mode == "unlock") {
            if (passphrase.isBlank()) {
                errorMessage = "Please enter your passphrase."
                return
            }
            val result = onUnlock(passphrase)
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
            val result = onCreate(passphrase, migrateLegacy)
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
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Arlo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArloTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ArloDarkSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "PRIVATE LOCAL VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = if (mode == "unlock") "Unlock Arlo" else "Protect your Arlo data",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArloTextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (mode == "unlock")
                        "Your data is encrypted on this device. Enter your passphrase to continue."
                    else
                        "Create a passphrase to encrypt your local data. Arlo cannot recover it if you lose it.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ArloTextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Error Notice
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArloDangerContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, ArloDanger, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = ArloDanger,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Passphrase Input
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Passphrase (min 12 characters)") },
                    placeholder = { Text("At least 12 characters") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_passphrase_input"),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (mode == "unlock") ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (mode == "unlock") submit() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = ArloTextMuted
                            )
                        }
                    },
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
                        label = { Text("Confirm passphrase") },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasLegacyData) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = migrateLegacy,
                                onCheckedChange = { migrateLegacy = it },
                                colors = CheckboxDefaults.colors(checkedColor = ArloPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Import my existing local prototype data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArloTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mode == "unlock") "Unlock securely" else "Create encrypted vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode toggle or Reset option
                if (hasExistingVault) {
                    if (mode == "unlock") {
                        TextButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.testTag("vault_reset_button")
                        ) {
                            Text("Reset vault / start fresh", color = ArloDanger)
                        }
                    } else {
                        TextButton(onClick = { mode = "unlock" }) {
                            Text("Back to unlock", color = ArloPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Arlo uses AES-256-GCM locally. Keep this passphrase safe; it is never sent to a server.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArloTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Encrypted Vault?") },
            text = {
                Text(
                    "This will delete your local encrypted vault permanently. Any stored goals, tasks, reflections, and notes will be cleared.",
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
                    Text("Delete and Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
