package com.example.arlo.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.arlo.MainActivity
import com.example.arlo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class CatTone(val label: String, val icon: String, val quipPrefix: String) {
    GENTLE_MEOW("Gentle Meow", "🐾", "Meow! Just a gentle nudge from Arlo: "),
    BISCUIT_KNEADER("Biscuit Kneader", "🍪", "Arlo is baking biscuits! Time to focus on: "),
    FELINE_ZOOMIES("Feline Zoomies", "⚡", "Zoomies time! Pounce on this goal: "),
    ZEN_LOAF("Zen Cat Loaf", "🧘", "Loaf peacefully and take a mindful moment for: ")
}

data class ScheduledCatReminder(
    val id: String,
    val title: String,
    val message: String,
    val triggerTimeEpochMs: Long,
    val category: String, // "Task", "Reflection", "Custom"
    val tone: CatTone,
    val playSound: Boolean = true
)

class CatNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "arlo_cat_reminders_channel"
        const val CHANNEL_NAME = "Arlo Cat Reminders & Purrs"
        const val CHANNEL_DESC = "Playful feline notifications, task reminders, and mindful purr check-ins"
        const val PREFS_NAME = "arlo_cat_reminders_prefs"
        const val KEY_REMINDERS_JSON = "active_cat_reminders"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _remindersFlow = MutableStateFlow<List<ScheduledCatReminder>>(emptyList())
    val remindersFlow: StateFlow<List<ScheduledCatReminder>> = _remindersFlow.asStateFlow()

    init {
        createNotificationChannel()
        loadReminders()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Cat purr vibration pattern: short purr pulses
            val purrVibrationPattern = longArrayOf(0, 150, 70, 150, 70, 300, 100, 200)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = purrVibrationPattern
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadReminders() {
        val jsonStr = prefs.getString(KEY_REMINDERS_JSON, "[]") ?: "[]"
        val list = mutableListOf<ScheduledCatReminder>()
        try {
            val arr = JSONArray(jsonStr)
            val now = System.currentTimeMillis()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val trigger = obj.getLong("triggerTimeEpochMs")
                if (trigger > now - 60000) { // keep if in future or very recent
                    list.add(
                        ScheduledCatReminder(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            message = obj.getString("message"),
                            triggerTimeEpochMs = trigger,
                            category = obj.getString("category"),
                            tone = CatTone.valueOf(obj.optString("tone", CatTone.GENTLE_MEOW.name)),
                            playSound = obj.optBoolean("playSound", true)
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        _remindersFlow.value = list
    }

    private fun saveReminders(list: List<ScheduledCatReminder>) {
        _remindersFlow.value = list
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("message", r.message)
                    put("triggerTimeEpochMs", r.triggerTimeEpochMs)
                    put("category", r.category)
                    put("tone", r.tone.name)
                    put("playSound", r.playSound)
                }
            )
        }
        prefs.edit().putString(KEY_REMINDERS_JSON, arr.toString()).apply()
    }

    fun scheduleReminder(
        title: String,
        targetTaskOrReflection: String,
        delayMillis: Long,
        category: String,
        tone: CatTone,
        playSound: Boolean = true
    ): ScheduledCatReminder {
        val id = "cat_rem_${System.currentTimeMillis()}_${(100..999).random()}"
        val triggerTime = System.currentTimeMillis() + delayMillis
        val playfulMessage = "${tone.quipPrefix}$targetTaskOrReflection"

        val reminder = ScheduledCatReminder(
            id = id,
            title = "${tone.icon} $title",
            message = playfulMessage,
            triggerTimeEpochMs = triggerTime,
            category = category,
            tone = tone,
            playSound = playSound
        )

        val intent = Intent(context, CatNotificationReceiver::class.java).apply {
            putExtra("reminder_id", id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_message", reminder.message)
            putExtra("play_sound", playSound)
            putExtra("tone_name", tone.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        val updated = _remindersFlow.value + reminder
        saveReminders(updated)
        return reminder
    }

    fun cancelReminder(id: String) {
        val intent = Intent(context, CatNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        val updated = _remindersFlow.value.filter { it.id != id }
        saveReminders(updated)
    }

    fun triggerInstantNotification(
        title: String = "🐾 Meow! Arlo Check-in",
        message: String = "Arlo is purring gently. Time to take a mindful breath and check your sprint tasks!",
        tone: CatTone = CatTone.GENTLE_MEOW,
        playSound: Boolean = true
    ) {
        if (playSound) {
            playFelineChime()
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val purrVibrationPattern = longArrayOf(0, 150, 70, 150, 70, 300, 100, 200)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${tone.icon} $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(purrVibrationPattern)
            .addAction(R.mipmap.ic_launcher, "Open Vault 🐾", pendingIntent)
            .build()

        notificationManager.notify((1000..9999).random(), notification)
    }

    fun playFelineChime() {
        try {
            // Play a cheerful high-pitched feline purr/chirp sound using ToneGenerator
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (_: Exception) {}
    }
}
