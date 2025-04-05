package com.joy.mytaskmanager.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joy.mytaskmanager.dao.TaskDao
import com.joy.mytaskmanager.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(taskDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainViewModel(private val taskDao: TaskDao) : ViewModel() {
    val tasks: LiveData<List<Task>> = taskDao.allTasks()

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
            taskDao.update(task)
            _selectedTask.emit(taskDao.task(task.id))
        }
    }

    fun addTask(newTask: Task) {
        Log.i("MainViewModel", "addTask(): $newTask")
        viewModelScope.launch {
            taskDao.insert(newTask)
        }
    }
}