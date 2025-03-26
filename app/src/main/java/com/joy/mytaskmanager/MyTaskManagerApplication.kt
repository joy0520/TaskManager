package com.joy.mytaskmanager

import android.app.Application
import com.joy.mytaskmanager.db.TaskDb

class MyTaskManagerApplication : Application() {
    val taskDb: TaskDb by lazy { TaskDb.getDatabase(this) }
}