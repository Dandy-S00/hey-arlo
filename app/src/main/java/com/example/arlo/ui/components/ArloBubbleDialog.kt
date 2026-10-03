package com.example.arlo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.ui.theme.*

@Composable
fun ArloBubbleButton(
    avatarSymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .testTag("arlo_bubble_fab")
            .size(56.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Pulse ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulseScale)
                .background(ArloPrimary.copy(alpha = 0.2f), CircleShape)
        )
        // Main bubble
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(ArloDarkSurface, CircleShape)
                .border(2.dp, ArloBorderHighlight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarSymbol,
                fontSize = 24.sp,
                color = ArloPrimary,
                fontWeight = FontWeight.Bold
            )
            // Green listening dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .background(ArloSuccess, CircleShape)
                    .border(1.5.dp, ArloDarkBackground, CircleShape)
            )
        }
    }
}

@Composable
fun ArloBubbleDialog(
    currentAvatar: String,
    onAvatarChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("✦", "☼", "◈", "☁", "🌿", "⭐")
    var customInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(ArloSuccess, CircleShape)
                )
                Text("Arlo is active", color = ArloTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Arlo is looking and listening only through sources you have enabled. No source is active unless you turn it on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArloTextSecondary
                )
                Surface(
                    color = ArloDarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 No source is active unless you explicitly turn it on in Privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose companion avatar:",
                    style = MaterialTheme.typography.titleSmall,
                    color = ArloTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    options.forEach { symbol ->
                        val isSelected = currentAvatar == symbol
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isSelected) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                                    CircleShape
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ArloPrimary else ArloBorder,
                                    shape = CircleShape
                                )
                                .clickable { onAvatarChange(symbol) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = symbol, fontSize = 20.sp, color = ArloTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ArloPrimary, contentColor = ArloOnPrimary)
            ) {
                Text("Close")
            }
        },
        containerColor = ArloDarkSurface
    )
}
