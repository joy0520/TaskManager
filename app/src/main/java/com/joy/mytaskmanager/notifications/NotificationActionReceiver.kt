package com.joy.mytaskmanager.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.joy.mytaskmanager.MyTaskManagerApplication
import com.joy.mytaskmanager.repository.TaskRepository
import com.joy.mytaskmanager.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles click from buttons on a notification.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "DISABLE_NOTIFICATION") {
            val taskId = intent.getLongExtra("EXTRA_TASK_ID", -1).toInt()
            if (taskId == -1) return

            onReceiveTaskNotificationDisable(context, taskId)
        }
    }

    private fun onReceiveTaskNotificationDisable(context: Context, taskId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            disableTaskNotification(context, taskId)

            // cancel the notification from the status bar
            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.cancel(taskId)

            Log.w(TAG, "Task ID=$taskId disabled & notification canceled")
        }
    }

    private suspend fun disableTaskNotification(context: Context, taskId: Int) {
        val application = context.applicationContext as MyTaskManagerApplication
        val taskDao = application.taskDb.taskDao()
        val repository = TaskRepositoryImpl(taskDao, application)

        repository.disableNotificationForTask(taskId)
    }
}