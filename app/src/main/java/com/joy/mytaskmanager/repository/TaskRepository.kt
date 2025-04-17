package com.joy.mytaskmanager.repository

import androidx.lifecycle.LiveData
import com.joy.mytaskmanager.data.Task

interface TaskRepository {
    fun allTasks(): LiveData<List<Task>>
    suspend fun task(taskId: Int): Task?
    suspend fun add(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(taskId: Int)
    suspend fun disableNotificationForTask(taskId: Int)
}