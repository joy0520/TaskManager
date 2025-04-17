package com.joy.mytaskmanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.joy.mytaskmanager.db.TaskDb

class MyTaskManagerApplication : Application() {
    val taskDb: TaskDb by lazy { TaskDb.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.notification_channel_name)
        val description = getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("TASK_REMINDERS", name, importance).apply {
            this.description = description
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}