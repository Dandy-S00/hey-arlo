package com.example.arlo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectedAppSource(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val category: String,
    val isConnected: Boolean,
    val lastSyncTime: String,
    val informationGathered: List<String>,
    val privacyDestination: String,
    val itemsSyncedCount: Int
)

class ConnectedAppsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_connected_apps_prefs", Context.MODE_PRIVATE)

    private val _sourcesFlow = MutableStateFlow(loadSources())
    val sourcesFlow: StateFlow<List<ConnectedAppSource>> = _sourcesFlow.asStateFlow()

    private fun loadSources(): List<ConnectedAppSource> {
        val calendarConnected = prefs.getBoolean("app_calendar", true)
        val keepConnected = prefs.getBoolean("app_keep", true)
        val healthConnected = prefs.getBoolean("app_health", true)
        val wellbeingConnected = prefs.getBoolean("app_wellbeing", true)
        val spotifyConnected = prefs.getBoolean("app_spotify", false)
        val weatherConnected = prefs.getBoolean("app_weather", true)

        return listOf(
            ConnectedAppSource(
                id = "google_calendar",
                name = "Google Calendar",
                iconEmoji = "📅",
                category = "Schedule & Rhythm",
                isConnected = calendarConnected,
                lastSyncTime = if (calendarConnected) "12 minutes ago" else "Disconnected",
                informationGathered = listOf(
                    "Event start & finish timestamps",
                    "Focus block durations & meeting density",
                    "Morning & evening routine consistency",
                    "Filtered: Attendee emails and private body notes are excluded"
                ),
                privacyDestination = "Stored in local on-device encrypted vault (AES-256-GCM). Never shared externally.",
                itemsSyncedCount = if (calendarConnected) 5 else 0
            ),
            ConnectedAppSource(
                id = "google_keep_notes",
                name = "Google Keep & Markdown Notes",
                iconEmoji = "📝",
                category = "Reflections & Thoughts",
                isConnected = keepConnected,
                lastSyncTime = if (keepConnected) "24 minutes ago" else "Disconnected",
                informationGathered = listOf(
                    "Journal reflection snippets & user thoughts",
                    "Daily intention checklists",
                    "Mood keywords for behavioral pattern analysis"
                ),
                privacyDestination = "Encrypted on-device in your private Arlo vault. Zero external data transmission.",
                itemsSyncedCount = if (keepConnected) 8 else 0
            ),
            ConnectedAppSource(
                id = "health_connect_fit",
                name = "Google Fit & Health Connect",
                iconEmoji = "🏃",
                category = "Physical Energy & Vitality",
                isConnected = healthConnected,
                lastSyncTime = if (healthConnected) "Just now" else "Disconnected",
                informationGathered = listOf(
                    "Daily step counts & active movement pace",
                    "Resting heart rate trends",
                    "Sleep duration (hours) and wake-up consistency",
                    "Calibrates your morning energy battery score"
                ),
                privacyDestination = "Stored strictly in device secure storage. Never uploaded to ad networks.",
                itemsSyncedCount = if (healthConnected) 7420 else 0
            ),
            ConnectedAppSource(
                id = "digital_wellbeing",
                name = "Device Clock & Digital Wellbeing",
                iconEmoji = "⏱️",
                category = "Screen Rhythm & Rest",
                isConnected = wellbeingConnected,
                lastSyncTime = if (wellbeingConnected) "45 minutes ago" else "Disconnected",
                informationGathered = listOf(
                    "Evening wind-down & phone sleep-time",
                    "First device pickup time in the morning",
                    "Focus app usage vs recreational screen time"
                ),
                privacyDestination = "Analyzed in device local RAM only. Never exported.",
                itemsSyncedCount = if (wellbeingConnected) 4 else 0
            ),
            ConnectedAppSource(
                id = "spotify_media",
                name = "Spotify & Media Playback",
                iconEmoji = "🎵",
                category = "Focus Soundscapes",
                isConnected = spotifyConnected,
                lastSyncTime = if (spotifyConnected) "Playing: Lo-fi Beats" else "Disconnected",
                informationGathered = listOf(
                    "Active playback genre (e.g. Ambient, Lo-fi, Classical)",
                    "Music tempo (used to detect deep work vs relax state)"
                ),
                privacyDestination = "Used ephemerally during focus sessions. Zero storage.",
                itemsSyncedCount = if (spotifyConnected) 1 else 0
            ),
            ConnectedAppSource(
                id = "device_weather_location",
                name = "Device Weather & Ambient Location",
                iconEmoji = "⛅",
                category = "Atmosphere & Day/Night",
                isConnected = weatherConnected,
                lastSyncTime = if (weatherConnected) "Live" else "Disconnected",
                informationGathered = listOf(
                    "Current local weather (Sun, Rain, Clouds, Snow)",
                    "Outdoor temperature & barometric trend",
                    "Sunrise & sunset times for cat sleep cycles"
                ),
                privacyDestination = "Used locally to adapt Arlo's dynamic icons, story ring, and greetings.",
                itemsSyncedCount = if (weatherConnected) 1 else 0
            )
        )
    }

    fun toggleSource(sourceId: String) {
        val key = when (sourceId) {
            "google_calendar" -> "app_calendar"
            "google_keep_notes" -> "app_keep"
            "health_connect_fit" -> "app_health"
            "digital_wellbeing" -> "app_wellbeing"
            "spotify_media" -> "app_spotify"
            "device_weather_location" -> "app_weather"
            else -> return
        }
        val current = prefs.getBoolean(key, true)
        prefs.edit().putBoolean(key, !current).apply()
        _sourcesFlow.value = loadSources()
    }

    fun syncAllNow() {
        _sourcesFlow.value = loadSources()
    }
}
