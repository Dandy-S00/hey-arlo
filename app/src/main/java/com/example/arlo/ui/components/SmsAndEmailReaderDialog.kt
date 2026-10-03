package com.example.arlo.ui.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.data.SmsAndEmailManager
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SmsAndEmailReaderDialog(
    smsAndEmailManager: SmsAndEmailManager,
    onConvertToTask: (String) -> Unit,
    onConvertToGoal: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = SMS, 1 = Emails
    val smsList by smsAndEmailManager.smsListFlow.collectAsState()
    val emailList by smsAndEmailManager.emailListFlow.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(smsAndEmailManager.hasSmsPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            scope.launch {
                smsAndEmailManager.refreshSmsMessages()
            }
        } else {
            Toast.makeText(context, "SMS permission needed to read text messages", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            smsAndEmailManager.refreshSmsMessages()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
            color = ArloDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(ArloPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (activeTab == 0) "💬" else "✉️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Messages & Communications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArloTextPrimary
                            )
                            Text(
                                text = "Read text messages & connected emails",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ArloTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher (SMS vs Emails)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArloDarkSurfaceVariant, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    listOf("💬 SMS & Texts", "✉️ Connected Emails").forEachIndexed { index, label ->
                        val isSelected = activeTab == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ArloPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = index }
                                .testTag("msg_tab_$index")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ArloOnPrimary else ArloTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SMS Tab Content
                if (activeTab == 0) {
                    if (!hasPermission) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ArloDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔒", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Permission Required",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ArloTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Grant SMS read permission to securely scan incoming texts and turn action items into sprint tasks.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArloTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("grant_sms_permission_button")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Grant SMS Access", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT INBOX (${smsList.size} SMS)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isRefreshing = true
                                        smsAndEmailManager.refreshSmsMessages()
                                        isRefreshing = false
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ArloPrimary)
                            }
                        }

                        if (smsList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No SMS messages found in inbox.", color = ArloTextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(smsList) { sms ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ArloDarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("sms_item_${sms.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = sms.sender,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArloPrimary
                                                )
                                                Text(
                                                    text = sms.dateFormatted,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ArloTextMuted,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = sms.body,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ArloTextPrimary,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        onConvertToTask(sms.body)
                                                        Toast.makeText(context, "Converted SMS to Task!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("⚡ To Task", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = {
                                                        onConvertToGoal(sms.body)
                                                        Toast.makeText(context, "Converted SMS to Goal!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloSuccess),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("🎯 To Goal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Emails Tab Content
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONNECTED INBOX (${emailList.size} EMAILS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloWarmGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(emailList) { email ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ArloDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder),
                                modifier = Modifier.fillMaxWidth().testTag("email_item_${email.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = email.sender,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArloWarmGold
                                        )
                                        Text(
                                            text = email.dateFormatted,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ArloTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = email.subject,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArloTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = email.snippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                onConvertToTask(email.subject)
                                                Toast.makeText(context, "Converted Email to Task!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("⚡ To Task", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                onConvertToGoal(email.subject)
                                                Toast.makeText(context, "Converted Email to Goal!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ArloDarkSurface, contentColor = ArloSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("🎯 To Goal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
}
