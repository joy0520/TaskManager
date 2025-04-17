package com.joy.mytaskmanager.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joy.mytaskmanager.dao.TaskDao
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.repository.TaskRepository
import com.joy.mytaskmanager.repository.TaskRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModelFactory(
    private val taskDao: TaskDao,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Repository instance
            val repository: TaskRepository = TaskRepositoryImpl(taskDao, applicationContext)

            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    val tasks: LiveData<List<Task>> = taskRepository.allTasks()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    fun selectTask(taskId: Int) {
        Log.i("MainViewModel", "selectTask() $taskId")
        viewModelScope.launch {
            _selectedTask.emit(tasks.value?.find { it.id == taskId })
        }
    }

    fun unselectCurrentTask() {
        Log.i("MainViewModel", "unselectCurrentTask()")
        viewModelScope.launch {
            _selectedTask.emit(null)
        }
    }

    fun updateTask(task: Task) {
        Log.i("MainViewModel", "updateTask(): $task")
        viewModelScope.launch {
            taskRepository.update(task)
            _selectedTask.emit(taskRepository.task(task.id))
        }
    }

    fun addTask(newTask: Task) {
        Log.i("MainViewModel", "addTask(): $newTask")
        viewModelScope.launch {
            taskRepository.add(newTask)
        }
    }

    fun deleteTask(taskId: Int) {
        Log.i("MainViewModel", "deleteTask(): ID=$taskId")
        viewModelScope.launch {
            taskRepository.delete(taskId)
        }
    }
}