package com.example.arlo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.ui.theme.*

@Composable
fun CatBackgroundPaws(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "paws_float")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paw_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val pawColor = ArloPrimary.copy(alpha = alphaAnim)

        // Draw decorative paw prints at fixed organic locations
        fun drawPaw(x: Float, y: Float, scale: Float = 1f) {
            val r = 14f * scale
            // Main pad
            drawCircle(color = pawColor, radius = r, center = Offset(x, y))
            // 4 toe beans
            drawCircle(color = pawColor, radius = r * 0.45f, center = Offset(x - r * 1.1f, y - r * 0.9f))
            drawCircle(color = pawColor, radius = r * 0.48f, center = Offset(x - r * 0.4f, y - r * 1.3f))
            drawCircle(color = pawColor, radius = r * 0.48f, center = Offset(x + r * 0.4f, y - r * 1.3f))
            drawCircle(color = pawColor, radius = r * 0.45f, center = Offset(x + r * 1.1f, y - r * 0.9f))
        }

        drawPaw(size.width * 0.12f, size.height * 0.15f, 0.9f)
        drawPaw(size.width * 0.88f, size.height * 0.35f, 1.1f)
        drawPaw(size.width * 0.18f, size.height * 0.65f, 0.8f)
        drawPaw(size.width * 0.82f, size.height * 0.85f, 1.0f)
    }
}

@Composable
fun InteractivePetArloWidget(
    tunaTreats: Int,
    onPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPetting by remember { mutableStateOf(false) }
    var purrCount by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "tail_wag")
    val tailRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_rotation"
    )

    val purrScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPetting) 1.12f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPetting) 400 else 1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "purr_scale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ArloDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimaryContainer),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isPetting = true
                purrCount++
                onPet()
            }
            .testTag("interactive_pet_arlo_card")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(purrScale)
                        .rotate(tailRotation)
                        .background(ArloPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPetting) "😽" else "🐱",
                        fontSize = 26.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPetting) "Arlo is Purring! Prrrrr..." else "Pet Arlo for Focus",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArloTextPrimary
                        )
                        if (purrCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("💖", fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = if (isPetting) "Biscuits baked with love • Focus calibrated" else "Tap gently to pet Arlo • Earn tuna treats",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArloTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ArloDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐟", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$tunaTreats",
                        color = ArloWarmGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
