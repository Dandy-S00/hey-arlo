package com.example.arlo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.app.NotificationCompat
import com.example.arlo.MainActivity
import com.example.arlo.R

class CatNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("reminder_title") ?: "🐾 Arlo Reminder"
        val message = intent.getStringExtra("reminder_message") ?: "Time for your scheduled reflection with Arlo!"
        val playSound = intent.getBooleanExtra("play_sound", true)

        if (playSound) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
            } catch (_: Exception) {}
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val purrVibrationPattern = longArrayOf(0, 150, 70, 150, 70, 300, 100, 200)

        val notification = NotificationCompat.Builder(context, CatNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(purrVibrationPattern)
            .addAction(R.mipmap.ic_launcher, "View Task 🐾", pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((1000..9999).random(), notification)
    }
}
