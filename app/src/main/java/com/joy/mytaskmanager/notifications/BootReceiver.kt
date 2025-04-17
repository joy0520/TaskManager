package com.joy.mytaskmanager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.db.TaskDb
import com.joy.mytaskmanager.util.scheduleNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reset `AlarmManager` after rebooting.
 */
class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.i(TAG, "Device booted, rescheduling alarms")
            onReceiveBootCompleted(context)
        }
    }

    private fun onReceiveBootCompleted(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            // fetch all tasks that needs to be alerted
            val tasks = allTasks(context)

            // schedule notification for all of them
            tasks.forEach { it.scheduleNotification(context) }
        }
    }

    private fun allTasks(context: Context): List<Task> {
        val taskDb: TaskDb = TaskDb.getDatabase(context.applicationContext)

        return taskDb.taskDao().allTasks().value ?: listOf()
    }
}