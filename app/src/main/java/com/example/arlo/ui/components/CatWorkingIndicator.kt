package com.example.arlo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.delay

object CatSayings {
    val WORKING_SAYINGS = listOf(
        "Sharpening claws and encrypting thoughts...",
        "Herding bits like unruly kittens...",
        "Baking biscuits on your local encrypted storage...",
        "Knocking unnecessary data off the digital counter...",
        "Chasing a mysterious red laser pointer in memory...",
        "Taking a quick 3-second nap, then finishing up...",
        "Making sure no sneaky dogs tamper with your vault...",
        "Sprinkling catnip over the algorithms...",
        "Tucking your data into a cozy cardboard box...",
        "Doing high-speed 3 AM zoomies through the database...",
        "Pawing at the screen to make it go faster..."
    )

    val DONE_SAYINGS = listOf(
        "Purr-fect! All done and securely stashed away.",
        "Meow-gical! Everything is saved and ready.",
        "Finished! Even caught that red laser dot for you.",
        "All sealed in the box. Now where are my treats?",
        "Done! Cleaned my paws and safely locked the vault.",
        "Paws of approval! Everything went smoothly."
    )
}

/**
 * Animated Cat illustration with interactive working and completion states.
 */
@Composable
fun AnimatedCatFace(
    isWorking: Boolean,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cat_animation")

    // Ear twitch oscillation
    val earRotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ear_twitch"
    )

    // Tail swish
    val tailAngle by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_swish"
    )

    // Kneading / typing paw movement (left paw)
    val leftPawOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "left_paw"
    )

    // Kneading / typing paw movement (right paw)
    val rightPawOffset by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "right_paw"
    )

    // Cat breathing bounce
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Eye blinking
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                1f at 0
                1f at 2200
                0.1f at 2300
                1f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_blink"
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(if (isDone) 1.08f else breathingScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            val strokeLavender = Color(0xFFF6B8FF)
            val strokeSecondary = Color(0xFFD4BBEE)
            val strokeGold = Color(0xFFFFD56B)
            val strokeColor = if (isDone) strokeGold else strokeLavender

            // 1. Swishing Tail in the background
            val tailPath = Path().apply {
                moveTo(cx + 36f, cy + 30f)
                val tailEndX = cx + 48f + (tailAngle * 0.4f)
                val tailEndY = cy + 5f + (tailAngle * 0.3f)
                quadraticTo(cx + 52f, cy + 18f, tailEndX, tailEndY)
            }
            drawPath(
                path = tailPath,
                color = strokeSecondary,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // 2. Cat Head Outline with ears
            val earWiggle = if (isDone) 0f else earRotation
            val headPath = Path().apply {
                // Top between ears
                moveTo(cx - 20f, cy - 22f)
                quadraticTo(cx, cy - 20f, cx + 20f, cy - 22f)

                // Right ear
                lineTo(cx + 38f + earWiggle, cy - 48f)
                lineTo(cx + 38f, cy - 12f)

                // Right cheek curve
                quadraticTo(cx + 50f, cy + 10f, cx + 34f, cy + 28f)

                // Chin
                quadraticTo(cx, cy + 36f, cx - 34f, cy + 28f)

                // Left cheek curve
                quadraticTo(cx - 50f, cy + 10f, cx - 38f, cy - 12f)

                // Left ear
                lineTo(cx - 38f - earWiggle, cy - 48f)
                close()
            }

            drawPath(
                path = headPath,
                color = strokeColor,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Inner ears
            drawLine(
                color = strokeSecondary,
                start = Offset(cx - 28f, cy - 36f),
                end = Offset(cx - 26f, cy - 16f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeSecondary,
                start = Offset(cx + 28f, cy - 36f),
                end = Offset(cx + 26f, cy - 16f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // 4. Eyes
            if (isDone) {
                // Happy curved celebrating eyes (^_^)
                val leftHappyEye = Path().apply {
                    moveTo(cx - 25f, cy - 2f)
                    quadraticTo(cx - 17f, cy - 12f, cx - 9f, cy - 2f)
                }
                val rightHappyEye = Path().apply {
                    moveTo(cx + 9f, cy - 2f)
                    quadraticTo(cx + 17f, cy - 12f, cx + 25f, cy - 2f)
                }
                drawPath(leftHappyEye, color = strokeGold, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                drawPath(rightHappyEye, color = strokeGold, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            } else {
                // Working focused eyes (with gentle blink)
                val eyeScaleY = eyeBlink.coerceIn(0.1f, 1f)
                drawOval(
                    color = strokeColor,
                    topLeft = Offset(cx - 22f, cy - 6f - (3f * eyeScaleY)),
                    size = androidx.compose.ui.geometry.Size(9f, 8f * eyeScaleY)
                )
                drawOval(
                    color = strokeColor,
                    topLeft = Offset(cx + 13f, cy - 6f - (3f * eyeScaleY)),
                    size = androidx.compose.ui.geometry.Size(9f, 8f * eyeScaleY)
                )
            }

            // 5. Nose & Mouth
            val noseY = cy + 8f
            drawCircle(
                color = strokeColor,
                radius = 3f,
                center = Offset(cx, noseY)
            )

            // Cute smiling mouth arcs
            val mouthLeft = Path().apply {
                moveTo(cx, noseY + 2f)
                quadraticTo(cx - 6f, noseY + 8f, cx - 11f, noseY + 5f)
            }
            val mouthRight = Path().apply {
                moveTo(cx, noseY + 2f)
                quadraticTo(cx + 6f, noseY + 8f, cx + 11f, noseY + 5f)
            }
            drawPath(mouthLeft, color = strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            drawPath(mouthRight, color = strokeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            // 6. Whiskers
            val whiskerColor = strokeSecondary
            // Left whiskers
            drawLine(whiskerColor, Offset(cx - 30f, cy + 6f), Offset(cx - 48f, cy + 2f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            drawLine(whiskerColor, Offset(cx - 30f, cy + 12f), Offset(cx - 46f, cy + 14f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            // Right whiskers
            drawLine(whiskerColor, Offset(cx + 30f, cy + 6f), Offset(cx + 48f, cy + 2f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            drawLine(whiskerColor, Offset(cx + 30f, cy + 12f), Offset(cx + 46f, cy + 14f), strokeWidth = 2.2f, cap = StrokeCap.Round)

            // 7. Kneading / Working Paws at bottom
            val pawColor = if (isDone) strokeGold else strokeLavender
            val lpY = cy + 32f + (if (isDone) 0f else leftPawOffset)
            val rpY = cy + 32f + (if (isDone) 0f else rightPawOffset)

            // Left paw
            drawOval(
                color = Color(0xFF1E1A29),
                topLeft = Offset(cx - 28f, lpY),
                size = androidx.compose.ui.geometry.Size(20f, 14f)
            )
            drawOval(
                color = pawColor,
                topLeft = Offset(cx - 28f, lpY),
                size = androidx.compose.ui.geometry.Size(20f, 14f),
                style = Stroke(width = 3f)
            )

            // Right paw
            drawOval(
                color = Color(0xFF1E1A29),
                topLeft = Offset(cx + 8f, rpY),
                size = androidx.compose.ui.geometry.Size(20f, 14f)
            )
            drawOval(
                color = pawColor,
                topLeft = Offset(cx + 8f, rpY),
                size = androidx.compose.ui.geometry.Size(20f, 14f),
                style = Stroke(width = 3f)
            )
        }
    }
}

/**
 * Full-featured working indicator overlay dialog with funny cat sayings,
 * playful feline animations, and celebration upon completion.
 */
@Composable
fun CatWorkingDialog(
    isWorking: Boolean,
    actionTitle: String = "Arlo is working...",
    isDone: Boolean = false,
    customDoneSaying: String? = null,
    onDismiss: () -> Unit
) {
    if (!isWorking && !isDone) return

    var currentSayingIndex by remember { mutableIntStateOf(0) }
    val sayings = CatSayings.WORKING_SAYINGS
    val doneSaying = remember(customDoneSaying) {
        customDoneSaying ?: CatSayings.DONE_SAYINGS.random()
    }

    // Cycle funny cat sayings every 1.7 seconds while working
    LaunchedEffect(isWorking, isDone) {
        while (isWorking && !isDone) {
            delay(1700)
            currentSayingIndex = (currentSayingIndex + 1) % sayings.size
        }
    }

    // Auto-dismiss after celebration if finished
    LaunchedEffect(isDone) {
        if (isDone) {
            delay(2200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isDone) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = isDone, dismissOnClickOutside = isDone)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("cat_working_indicator_card"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isDone) ArloWarmGold else ArloPrimary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Eyebrow Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDone) ArloWarmGold.copy(alpha = 0.2f) else ArloPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDone) "✨ PAWS-ITIVELY FINISHED" else "🐾 ARLO CAT AT WORK",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) ArloWarmGold else ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // The Animated Cat Face
                AnimatedCatFace(
                    isWorking = isWorking,
                    isDone = isDone,
                    modifier = Modifier.size(130.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Task Title
                Text(
                    text = if (isDone) "Task Completed!" else actionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = ArloTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // The Funny Cat Saying Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ArloDarkSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDone) ArloWarmGold.copy(alpha = 0.5f) else ArloBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isDone) "😼 CAT DISPATCH" else "💬 FELINE STATUS UPDATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) ArloWarmGold else ArloPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDone) "“$doneSaying”" else "“${sayings[currentSayingIndex]}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDone) ArloTextPrimary else ArloTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isDone) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("cat_done_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArloWarmGold,
                            contentColor = Color(0xFF221A00)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Purr-fect!", fontWeight = FontWeight.Bold)
                    }
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ArloPrimary,
                        trackColor = ArloDarkSurfaceVariant
                    )
                }
            }
        }
    }
}
