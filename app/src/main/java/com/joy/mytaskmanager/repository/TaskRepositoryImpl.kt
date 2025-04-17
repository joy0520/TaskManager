package com.joy.mytaskmanager.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.joy.mytaskmanager.dao.TaskDao
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.util.cancelNotification
import com.joy.mytaskmanager.util.scheduleNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val applicationContext: Context
) : TaskRepository {
    override fun allTasks(): LiveData<List<Task>> = taskDao.allTasks()
    override suspend fun task(taskId: Int): Task? = taskDao.task(taskId)

    override suspend fun add(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.insert(task)
            task.scheduleNotification(applicationContext)
        }
    }

    override suspend fun update(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.update(task)
            task.scheduleNotification(applicationContext)
        }
    }

    override suspend fun delete(taskId: Int) {
        withContext(Dispatchers.IO) {
            val task = taskDao.task(taskId) ?: return@withContext

            task.cancelNotification(applicationContext)
            taskDao.delete(taskId)
        }
    }

    override suspend fun disableNotificationForTask(taskId: Int) {
        withContext(Dispatchers.IO) {
            val task = taskDao.task(taskId) ?: return@withContext
            if (!task.isNotificationEnabled) return@withContext

            val newTask = task.copy(isNotificationEnabled = false)
            taskDao.update(newTask)
            newTask.cancelNotification(applicationContext)
        }
    }
}