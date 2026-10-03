package com.example.arlo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.window.Dialog
import com.example.arlo.data.AmbientAtmosphere
import com.example.arlo.data.AmbientWeatherManager
import com.example.arlo.data.WeatherCondition
import com.example.arlo.ui.theme.*

@Composable
fun AmbientWeatherDialog(
    ambientWeatherManager: AmbientWeatherManager,
    onDismiss: () -> Unit
) {
    val atmosphere by ambientWeatherManager.atmosphereFlow.collectAsState()
    var selectedWeather by remember(atmosphere) { mutableStateOf(atmosphere.weather) }
    var tempF by remember(atmosphere) { mutableStateOf(atmosphere.temperatureF) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .testTag("ambient_weather_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ArloDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArloPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(atmosphere.timeOfDay.icon, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Local Weather & Time of Day",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArloTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Arlo's icons adapt dynamically to your environment",
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

                // Current Active Atmosphere Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, atmosphere.timeOfDay.themeAccent.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(atmosphere.timeOfDay.icon, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(atmosphere.weather.icon, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${atmosphere.timeOfDay.title} • ${atmosphere.weather.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ArloTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Current Temp: ${atmosphere.temperatureF}°F",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = atmosphere.timeOfDay.themeAccent
                                    )
                                }
                            }

                            if (!atmosphere.isAutoDetected) {
                                TextButton(onClick = { ambientWeatherManager.resetToAutoDetection() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArloPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto", color = ArloPrimary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🐾 ${atmosphere.timeOfDay.felineStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather Condition Switcher
                Text(
                    text = "SELECT WEATHER CONDITION & ATMOSPHERE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArloPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WeatherCondition.entries) { cond ->
                        val isSelected = selectedWeather == cond
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ArloPrimaryContainer else ArloDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) ArloPrimary else ArloBorder
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedWeather = cond
                                    tempF = cond.defaultTempF
                                    ambientWeatherManager.setManualWeather(cond, cond.defaultTempF)
                                }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cond.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = cond.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArloTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${cond.defaultTempF}°F",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArloTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimary,
                        contentColor = ArloOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Atmosphere", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
