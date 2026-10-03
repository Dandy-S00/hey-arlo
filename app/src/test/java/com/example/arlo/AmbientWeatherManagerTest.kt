package com.example.arlo

import com.example.arlo.data.AmbientAtmosphere
import com.example.arlo.data.TimeOfDay
import com.example.arlo.data.WeatherCondition
import org.junit.Assert.*
import org.junit.Test

class AmbientWeatherManagerTest {

    @Test
    fun testTimeOfDayIconsAndTitles() {
        val dawn = TimeOfDay.DAWN
        assertEquals("🌅", dawn.icon)
        assertTrue(dawn.title.contains("Dawn"))

        val morning = TimeOfDay.MORNING
        assertEquals("☀️", morning.icon)

        val night = TimeOfDay.NIGHT
        assertEquals("🌙", night.icon)

        TimeOfDay.entries.forEach { tod ->
            assertTrue(tod.icon.isNotBlank())
            assertTrue(tod.felineStatus.isNotBlank())
        }
    }

    @Test
    fun testWeatherConditionsIconsAndTemps() {
        val sunny = WeatherCondition.SUNNY
        assertEquals("☀️", sunny.icon)
        assertTrue(sunny.defaultTempF > 60)

        val rainy = WeatherCondition.RAINY
        assertEquals("🌧️", rainy.icon)

        val snowy = WeatherCondition.SNOWY
        assertEquals("❄️", snowy.icon)

        WeatherCondition.entries.forEach { cond ->
            assertTrue(cond.icon.isNotBlank())
            assertTrue(cond.title.isNotBlank())
            assertTrue(cond.felineWeatherBadge.isNotBlank())
        }
    }

    @Test
    fun testTodayModuleIconShowsRainWhenRaining() {
        // If it's raining or stormy, the Today module icon MUST show the rain icon
        val rainyMorning = AmbientAtmosphere(
            timeOfDay = TimeOfDay.MORNING,
            weather = WeatherCondition.RAINY
        )
        assertEquals("🌧️", rainyMorning.todayModuleIcon)

        val stormyNight = AmbientAtmosphere(
            timeOfDay = TimeOfDay.NIGHT,
            weather = WeatherCondition.STORMY
        )
        assertEquals("🌧️", stormyNight.todayModuleIcon)
    }

    @Test
    fun testTodayModuleIconFitsTimeOfDayWhenNotRaining() {
        // When not raining, Today module icon fits the actual time of day
        val clearDawn = AmbientAtmosphere(timeOfDay = TimeOfDay.DAWN, weather = WeatherCondition.SUNNY)
        assertEquals("🌅", clearDawn.todayModuleIcon)

        val sunnyMorning = AmbientAtmosphere(timeOfDay = TimeOfDay.MORNING, weather = WeatherCondition.SUNNY)
        assertEquals("☀️", sunnyMorning.todayModuleIcon)

        val cloudyAfternoon = AmbientAtmosphere(timeOfDay = TimeOfDay.AFTERNOON, weather = WeatherCondition.PARTLY_CLOUDY)
        assertEquals("🌤️", cloudyAfternoon.todayModuleIcon)

        val goldenHour = AmbientAtmosphere(timeOfDay = TimeOfDay.GOLDEN_HOUR, weather = WeatherCondition.SUNNY)
        assertEquals("🌇", goldenHour.todayModuleIcon)

        val clearNight = AmbientAtmosphere(timeOfDay = TimeOfDay.NIGHT, weather = WeatherCondition.SUNNY)
        assertEquals("🌙", clearNight.todayModuleIcon)

        val deepMidnight = AmbientAtmosphere(timeOfDay = TimeOfDay.DEEP_NIGHT, weather = WeatherCondition.SUNNY)
        assertEquals("🌌", deepMidnight.todayModuleIcon)
    }
}
