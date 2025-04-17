package com.joy.mytaskmanager.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.notifications.AlarmReceiver
import java.time.Duration
import java.time.ZonedDateTime

/**
 * Should be called after a Task creation or edition.
 */
fun Task.scheduleNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Cancel any existing alarm for this task first
    cancelNotification(context)

    if (!isNotificationEnabled) {
        return // Do not schedule if disabled or no end time
    }

    val triggerAt = end - Duration.ofMinutes(30)
    val now = ZonedDateTime.now()
    // not yet the time to trigger, or the due has passed
    if (triggerAt <= now && now < end) {
        Log.w("scheduleNotification", "Task $id skipped to schedule a notification")
        return
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "TASK_DUE" // Define a custom action
        putExtra("EXTRA_TASK_ID", id) // Pass task ID
    }

    // Use a unique request code for each task's alarm PendingIntent
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        id.hashCode(), // Use task ID hash code as request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Check for SCHEDULE_EXACT_ALARM permission before calling setExact...
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt.toInstant().toEpochMilli(),
                pendingIntent
            )
        } else {
            // Handle the case where permission is not granted
            // Maybe schedule a less precise alarm or guide user to settings
            // alarmManager.setWindow(...) // Alternative inexact alarm
            Log.w(
                "scheduleNotification",
                "Cannot schedule exact alarms. Guide user to settings."
            )
            // Optionally fall back to an inexact alarm
            // alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    } else {
        // For older versions or if exact alarm permission is not needed/granted
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toInstant().toEpochMilli(),
            pendingIntent
        )
    }
    Log.d("scheduleNotification", "Scheduled notification for task $id at $triggerAt")
}

/**
 * Should be called after a task deletion or completion.
 */
fun Task.cancelNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "TASK_DUE"
        // IMPORTANT: The intent details (action, extras IF THEY WERE USED IN COMPARISON FILTER) must match the original
        // For matching based on request code, just need the same context, request code, and intent class
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        id.hashCode(), // Must match the request code used for scheduling
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE to check existence, then cancel
    )

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // Also cancel the PendingIntent itself
        Log.d("cancelNotification", "Cancelled notification for task $id")
    }
}