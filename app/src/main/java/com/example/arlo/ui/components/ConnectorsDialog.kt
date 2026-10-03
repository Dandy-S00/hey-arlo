package com.example.arlo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.model.ApiProvider
import com.example.arlo.model.ConnectionConsent
import com.example.arlo.model.ProviderCatalog
import com.example.arlo.ui.theme.*

@Composable
fun ConnectorsDialog(
    connections: Map<String, ConnectionConsent>,
    onRequestConnection: (ApiProvider) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var pendingConfirmProvider by remember { mutableStateOf<ApiProvider?>(null) }

    val filteredProviders = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ProviderCatalog.providers
        } else {
            val matched = ProviderCatalog.findProvider(searchQuery)
            if (matched != null) listOf(matched) else ProviderCatalog.providers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }.ifEmpty { listOf(ProviderCatalog.providers.last()) } // fallback to custom API
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = ArloDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERMISSION CENTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary
                        )
                        Text(
                            text = "Connect Arlo to an API",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose a service, or ask for one by name. Arlo will request only the access you approve.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_connectors_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ask: connect Google Calendar, Notion, or Weather API") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ArloPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ArloTextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connector_search_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArloPrimary,
                        unfocusedBorderColor = ArloBorder,
                        focusedTextColor = ArloTextPrimary,
                        unfocusedTextColor = ArloTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Provider List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProviders, key = { it.id }) { provider ->
                        val isAwaiting = connections[provider.id]?.status == "awaiting-provider-auth"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connector_row_${provider.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(ArloPrimaryContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = provider.logo,
                                        fontWeight = FontWeight.Black,
                                        color = ArloPrimary,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = provider.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ArloTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = provider.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = provider.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Auth: ${provider.auth}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        if (isAwaiting) {
                                            onRequestConnection(provider)
                                        } else {
                                            pendingConfirmProvider = provider
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAwaiting) ArloSecondaryContainer else ArloPrimary,
                                        contentColor = if (isAwaiting) ArloSecondary else ArloOnPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("connect_btn_${provider.id}")
                                ) {
                                    Text(
                                        text = if (isAwaiting) "Awaiting auth" else "Connect",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = ArloDarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ This catalog describes integrations only. Provider authorization and credentials are kept out of the catalog and must be handled by a secure adapter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextMuted,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }

    if (pendingConfirmProvider != null) {
        val provider = pendingConfirmProvider!!
        AlertDialog(
            onDismissRequest = { pendingConfirmProvider = null },
            title = { Text("Connect to ${provider.name}?") },
            text = {
                Text(
                    "Allow Arlo to start connecting to ${provider.name}? It will request only: ${provider.description}\n\nNo data is active until you approve the provider authorization step.",
                    color = ArloTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestConnection(provider)
                        pendingConfirmProvider = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
                ) {
                    Text("Approve Connection")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmProvider = null }) {
                    Text("Cancel", color = ArloTextSecondary)
                }
            },
            containerColor = ArloDarkSurface
        )
    }
}
