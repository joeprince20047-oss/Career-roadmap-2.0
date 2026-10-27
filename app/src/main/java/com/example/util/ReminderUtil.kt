package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object ReminderUtil {
  private const val CHANNEL_ID = "career_roadmap_reminders"
  private const val CHANNEL_NAME = "Daily Study Reminders"
  private const val PREFS_NAME = "roadmap_reminder_prefs"
  private const val KEY_ENABLED = "reminders_enabled"
  private const val KEY_HOUR = "reminder_hour"
  private const val KEY_MINUTE = "reminder_minute"

  fun isReminderEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_ENABLED, true)
  }

  fun getReminderTime(context: Context): Pair<Int, Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val hour = prefs.getInt(KEY_HOUR, 19) // default 7 PM
    val minute = prefs.getInt(KEY_MINUTE, 0)
    return hour to minute
  }

  fun setReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putBoolean(KEY_ENABLED, enabled)
      .putInt(KEY_HOUR, hour)
      .putInt(KEY_MINUTE, minute)
      .apply()

    createNotificationChannel(context)
  }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Daily reminders to maintain study streak and progress through your career roadmap."
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun sendTestNotification(context: Context, streak: Int, nextSkill: String?) {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val title = if (streak > 0) "🔥 Keep Your $streak-Day Streak Alive!" else "🎯 Time for Today's Study Session!"
    val content = if (nextSkill != null) {
      "Your next focus topic is $nextSkill. Log a quick 20-30 min study session today!"
    } else {
      "Open Career Roadmap to track your progress and level up your skills today."
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(NotificationCompat.BigTextStyle().bigText(content))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(1001, notification)
  }
}
