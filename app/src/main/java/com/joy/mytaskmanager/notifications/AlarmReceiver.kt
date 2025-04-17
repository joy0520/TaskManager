package com.joy.mytaskmanager.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.joy.mytaskmanager.MainActivity
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.db.TaskDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Called when a `AlarmManager` is triggered.
 */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "TASK_DUE") {
            onReceiveTaskDue(context, intent)
        }
    }

    private fun onReceiveTaskDue(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("EXTRA_TASK_ID", -1).toInt()
        if (taskId == -1) return

        Log.i(TAG, "Received alarm for task ID $taskId")
        CoroutineScope(Dispatchers.IO).launch {
            // perform DB operation inside the IO coroutine
            val task = taskFromDB(context, taskId) ?: return@launch

            showNotification(context, task)
        }
    }

    private suspend fun taskFromDB(context: Context, taskId: Int): Task? {
        val taskDb: TaskDb = TaskDb.getDatabase(context)

        return taskDb.taskDao().task(taskId)
    }

    private fun showNotification(context: Context, task: Task) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        // Intent to open the app when notification is tapped
        val contentIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Optional: Add extras to navigate to the specific task details screen
            putExtra("EXTRA_NAVIGATE_TO_TASK_ID", task.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode() + 1, // Unique request code
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for the "Don't notify again" action
        val disableActionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "DISABLE_NOTIFICATION"
            putExtra("EXTRA_TASK_ID", task.id)
        }
        val disableActionPendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode() + 2, // Unique request code
            disableActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            "TASK_REMINDERS"
        ) // Use the channel ID created earlier
            .setSmallIcon(R.drawable.ic_task) // Replace with your notification icon
            .setContentTitle("Task Due Soon: ${task.type.name}")
            .setContentText("Task '${task.description}' is due in 30 minutes.") // Or calculate remaining time
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent) // Set tap action
            .setAutoCancel(false) // Make it persistent (user must dismiss)
            .setOngoing(true) // Also helps persistence, shows in ongoing section
            .addAction(
                R.drawable.ic_stop_alert,
                "STOP ALERT",
                disableActionPendingIntent
            ) // Add action button
            .build()

        // Use task ID as notification ID to allow updating/cancelling
        notificationManager.notify(task.id.toInt(), notification) // Use task ID as notification ID
        Log.d("AlarmReceiver", "Notification shown for task ${task.id}")
    }
}