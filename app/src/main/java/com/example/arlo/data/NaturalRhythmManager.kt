package com.example.arlo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class Chronotype(val title: String, val icon: String, val description: String) {
    EARLY_LARK("Early Songbird", "🌅", "Peak mental energy from 6:30 AM to 11:30 AM. Best for tackling your highest-friction step first thing."),
    AFTERNOON_PROWLER("Afternoon Prowler", "☀️", "Peak focus from 12:30 PM to 6:00 PM. Best for midday deep-work sprints."),
    NIGHT_PANTHER("Night Owl Panther", "🌙", "Peak creativity and quiet immersion from 8:00 PM to 1:00 AM. Thrives with late-evening reflection."),
    STEADY_RHYTHM("Steady Flow Cat", "🐾", "Balanced energy distribution across the day. Adapts smoothly to gentle rolling check-ins.")
}

data class NaturalRhythmProfile(
    val isAutoTuneEnabled: Boolean = true,
    val chronotype: Chronotype = Chronotype.STEADY_RHYTHM,
    val learnedWakeHour: Int = 8,
    val learnedSleepHour: Int = 23,
    val peakFocusHour: Int = 10,
    val adaptiveHeartbeatHours: Int = 30,
    val totalRecordedInteractions: Int = 0,
    val hourlyActivityCounts: Map<Int, Int> = emptyMap(),
    val confidenceLevel: String = "Calibrating (3+ interactions)"
) {
    val activeWindowText: String
        get() {
            val wakeFormatted = formatHour(learnedWakeHour)
            val sleepFormatted = formatHour(learnedSleepHour)
            return "$wakeFormatted – $sleepFormatted"
        }

    val peakHourText: String
        get() = "${formatHour(peakFocusHour)} – ${formatHour((peakFocusHour + 2) % 24)}"

    private fun formatHour(h: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val hour12 = when (val mod = h % 12) {
            0 -> 12
            else -> mod
        }
        return "$hour12:00 $amPm"
    }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("isAutoTuneEnabled", isAutoTuneEnabled)
        put("chronotype", chronotype.name)
        put("learnedWakeHour", learnedWakeHour)
        put("learnedSleepHour", learnedSleepHour)
        put("peakFocusHour", peakFocusHour)
        put("adaptiveHeartbeatHours", adaptiveHeartbeatHours)
        put("totalRecordedInteractions", totalRecordedInteractions)
        val histObj = JSONObject()
        hourlyActivityCounts.forEach { (h, count) -> histObj.put(h.toString(), count) }
        put("hourlyActivityCounts", histObj)
        put("confidenceLevel", confidenceLevel)
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): NaturalRhythmProfile {
            if (json == null) return NaturalRhythmProfile()
            val histMap = mutableMapOf<Int, Int>()
            json.optJSONObject("hourlyActivityCounts")?.let { histObj ->
                val keys = histObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    histMap[key.toIntOrNull() ?: 0] = histObj.optInt(key, 0)
                }
            }

            val chronotypeEnum = try {
                Chronotype.valueOf(json.optString("chronotype", Chronotype.STEADY_RHYTHM.name))
            } catch (e: Exception) {
                Chronotype.STEADY_RHYTHM
            }

            return NaturalRhythmProfile(
                isAutoTuneEnabled = json.optBoolean("isAutoTuneEnabled", true),
                chronotype = chronotypeEnum,
                learnedWakeHour = json.optInt("learnedWakeHour", 8),
                learnedSleepHour = json.optInt("learnedSleepHour", 23),
                peakFocusHour = json.optInt("peakFocusHour", 10),
                adaptiveHeartbeatHours = json.optInt("adaptiveHeartbeatHours", 30),
                totalRecordedInteractions = json.optInt("totalRecordedInteractions", 0),
                hourlyActivityCounts = histMap,
                confidenceLevel = json.optString("confidenceLevel", "Calibrating (3+ interactions)")
            )
        }
    }
}

class NaturalRhythmManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_natural_rhythm_prefs", Context.MODE_PRIVATE)

    private val _rhythmProfileFlow = MutableStateFlow(loadProfile())
    val rhythmProfileFlow: StateFlow<NaturalRhythmProfile> = _rhythmProfileFlow.asStateFlow()

    private val recentTimestamps: MutableList<Long> = mutableListOf()

    init {
        loadRecentTimestamps()
        recomputeProfile()
    }

    private fun loadProfile(): NaturalRhythmProfile {
        val raw = prefs.getString("rhythm_profile_json", null) ?: return NaturalRhythmProfile()
        return try {
            NaturalRhythmProfile.fromJsonObject(JSONObject(raw))
        } catch (e: Exception) {
            NaturalRhythmProfile()
        }
    }

    private fun saveProfile(profile: NaturalRhythmProfile) {
        prefs.edit().putString("rhythm_profile_json", profile.toJsonObject().toString()).apply()
        _rhythmProfileFlow.value = profile
    }

    private fun loadRecentTimestamps() {
        val raw = prefs.getString("recent_interaction_timestamps", null) ?: return
        try {
            val arr = JSONArray(raw)
            recentTimestamps.clear()
            for (i in 0 until arr.length()) {
                recentTimestamps.add(arr.getLong(i))
            }
        } catch (e: Exception) {
            recentTimestamps.clear()
        }
    }

    private fun saveRecentTimestamps() {
        val arr = JSONArray()
        recentTimestamps.takeLast(60).forEach { arr.put(it) }
        prefs.edit().putString("recent_interaction_timestamps", arr.toString()).apply()
    }

    fun recordInteraction(eventType: String = "user_activity") {
        val now = System.currentTimeMillis()
        recentTimestamps.add(now)
        if (recentTimestamps.size > 60) {
            recentTimestamps.removeAt(0)
        }
        saveRecentTimestamps()
        recomputeProfile()
    }

    fun setAutoTuneEnabled(enabled: Boolean) {
        val updated = _rhythmProfileFlow.value.copy(isAutoTuneEnabled = enabled)
        saveProfile(updated)
    }

    private fun recomputeProfile() {
        if (recentTimestamps.isEmpty()) return

        val hourlyCounts = mutableMapOf<Int, Int>()
        val cal = Calendar.getInstance()

        recentTimestamps.forEach { ts ->
            cal.timeInMillis = ts
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourlyCounts[hour] = (hourlyCounts[hour] ?: 0) + 1
        }

        val total = recentTimestamps.size
        val peakHour = hourlyCounts.maxByOrNull { it.value }?.key ?: 10

        // Find active boundaries
        val hoursWithActivity = hourlyCounts.keys.sorted()
        val wakeHour = hoursWithActivity.firstOrNull { it in 5..12 } ?: 8
        val sleepHour = hoursWithActivity.lastOrNull { it in 19..23 || it in 0..3 } ?: 23

        // Determine Chronotype
        val morningCount = hourlyCounts.filterKeys { it in 5..11 }.values.sum()
        val afternoonCount = hourlyCounts.filterKeys { it in 12..17 }.values.sum()
        val nightCount = hourlyCounts.filterKeys { it in 18..23 || it in 0..4 }.values.sum()

        val chronotype = when {
            morningCount >= afternoonCount && morningCount >= nightCount && morningCount >= 3 -> Chronotype.EARLY_LARK
            nightCount >= morningCount && nightCount >= afternoonCount && nightCount >= 3 -> Chronotype.NIGHT_PANTHER
            afternoonCount >= morningCount && afternoonCount >= nightCount && afternoonCount >= 3 -> Chronotype.AFTERNOON_PROWLER
            else -> Chronotype.STEADY_RHYTHM
        }

        // Calculate average inter-checkup interval
        var adaptiveCadenceHours = 32
        if (recentTimestamps.size >= 4) {
            val diffs = mutableListOf<Long>()
            for (i in 1 until recentTimestamps.size) {
                val diffHours = (recentTimestamps[i] - recentTimestamps[i - 1]) / (1000 * 60 * 60)
                if (diffHours in 6..72) {
                    diffs.add(diffHours)
                }
            }
            if (diffs.isNotEmpty()) {
                diffs.sort()
                val median = diffs[diffs.size / 2].toInt()
                adaptiveCadenceHours = median.coerceIn(18, 48)
            }
        }

        val confidence = when {
            total < 5 -> "Calibrating (need ${5 - total} more check-ins)"
            total < 15 -> "Developing Pattern (fair accuracy)"
            else -> "High Accuracy (learned from $total interactions)"
        }

        val profile = NaturalRhythmProfile(
            isAutoTuneEnabled = _rhythmProfileFlow.value.isAutoTuneEnabled,
            chronotype = chronotype,
            learnedWakeHour = wakeHour,
            learnedSleepHour = sleepHour,
            peakFocusHour = peakHour,
            adaptiveHeartbeatHours = adaptiveCadenceHours,
            totalRecordedInteractions = total,
            hourlyActivityCounts = hourlyCounts,
            confidenceLevel = confidence
        )

        saveProfile(profile)
    }

    fun getAdaptiveCadenceIntervalMs(): Long {
        val current = _rhythmProfileFlow.value
        return if (current.isAutoTuneEnabled) {
            current.adaptiveHeartbeatHours * 60 * 60 * 1000L
        } else {
            32 * 60 * 60 * 1000L
        }
    }
}
