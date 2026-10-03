package com.example.arlo.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class TimeOfDay(
    val title: String,
    val icon: String,
    val felineStatus: String,
    val themeAccent: Color
) {
    DAWN("Early Dawn", "🌅", "Stretching paws in the first gentle light", Color(0xFFFFAB91)),
    MORNING("Morning Rhythm", "☀️", "Chasing dust motes in the morning sunbeam", Color(0xFFFFD54F)),
    AFTERNOON("Afternoon Flow", "🌤️", "Observant perching watch from the cat tree", Color(0xFF81D4FA)),
    GOLDEN_HOUR("Golden Dusk", "🌇", "Warm twilight purring & stretching", Color(0xFFFFB74D)),
    NIGHT("Night Reflection", "🌙", "Curled into a cozy velvet circle", Color(0xFFB388FF)),
    DEEP_NIGHT("Midnight Quiet", "🌌", "Deep peaceful slumber beside you", Color(0xFF7C4DFF))
}

enum class WeatherCondition(
    val title: String,
    val icon: String,
    val felineWeatherBadge: String,
    val defaultTempF: Int
) {
    SUNNY("Sunny & Clear", "☀️", "Warm Sunbeam Spot", 74),
    PARTLY_CLOUDY("Partly Cloudy", "⛅", "Gentle Breeze Whiskers", 68),
    RAINY("Rainy & Cozy", "🌧️", "Raindrop Window Watching", 59),
    STORMY("Thunderstorm", "⛈️", "Snuggled Safe Under Blankets", 55),
    SNOWY("Crisp & Snowy", "❄️", "Fluffy Winter Warmth", 32),
    FOGGY("Misty & Quiet", "🌫️", "Mysterious Velvet Fog", 52)
}

data class AmbientAtmosphere(
    val timeOfDay: TimeOfDay,
    val weather: WeatherCondition,
    val temperatureF: Int = 70,
    val isAutoDetected: Boolean = true
) {
    val compositeBadge: String
        get() = "${timeOfDay.icon} ${weather.icon} ${temperatureF}°F"

    val headerGreetingSubtext: String
        get() = "${timeOfDay.title} • ${weather.title} • ${timeOfDay.felineStatus}"

    val todayModuleIcon: String
        get() = if (weather == WeatherCondition.RAINY || weather == WeatherCondition.STORMY) {
            "🌧️"
        } else {
            timeOfDay.icon
        }
}

class AmbientWeatherManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_ambient_prefs", Context.MODE_PRIVATE)

    private val _atmosphereFlow = MutableStateFlow(calculateCurrentAtmosphere())
    val atmosphereFlow: StateFlow<AmbientAtmosphere> = _atmosphereFlow.asStateFlow()

    fun refreshAtmosphere(): AmbientAtmosphere {
        val current = calculateCurrentAtmosphere()
        _atmosphereFlow.value = current
        return current
    }

    fun setManualWeather(weather: WeatherCondition, tempF: Int) {
        prefs.edit()
            .putString("saved_weather", weather.name)
            .putInt("saved_temp", tempF)
            .putBoolean("manual_override", true)
            .apply()
        _atmosphereFlow.value = calculateCurrentAtmosphere()
    }

    fun resetToAutoDetection() {
        prefs.edit().remove("manual_override").apply()
        _atmosphereFlow.value = calculateCurrentAtmosphere()
    }

    private fun calculateCurrentAtmosphere(): AmbientAtmosphere {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val timeOfDay = when (hour) {
            in 5..8 -> TimeOfDay.DAWN
            in 9..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..19 -> TimeOfDay.GOLDEN_HOUR
            in 20..23 -> TimeOfDay.NIGHT
            else -> TimeOfDay.DEEP_NIGHT
        }

        val hasManual = prefs.getBoolean("manual_override", false)
        if (hasManual) {
            val weatherName = prefs.getString("saved_weather", WeatherCondition.SUNNY.name) ?: WeatherCondition.SUNNY.name
            val weather = WeatherCondition.entries.firstOrNull { it.name == weatherName } ?: WeatherCondition.SUNNY
            val temp = prefs.getInt("saved_temp", weather.defaultTempF)
            return AmbientAtmosphere(timeOfDay = timeOfDay, weather = weather, temperatureF = temp, isAutoDetected = false)
        }

        // Automatic weather heuristic based on month & time
        val month = cal.get(Calendar.MONTH) // 0-11
        val autoWeather = when {
            month in listOf(Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY) -> WeatherCondition.SNOWY
            month in listOf(Calendar.MARCH, Calendar.APRIL) -> WeatherCondition.RAINY
            hour in 11..16 -> WeatherCondition.SUNNY
            else -> WeatherCondition.PARTLY_CLOUDY
        }

        val temp = when (autoWeather) {
            WeatherCondition.SUNNY -> 75
            WeatherCondition.PARTLY_CLOUDY -> 68
            WeatherCondition.RAINY -> 60
            WeatherCondition.STORMY -> 56
            WeatherCondition.SNOWY -> 34
            WeatherCondition.FOGGY -> 50
        }

        return AmbientAtmosphere(
            timeOfDay = timeOfDay,
            weather = autoWeather,
            temperatureF = temp,
            isAutoDetected = true
        )
    }
}
