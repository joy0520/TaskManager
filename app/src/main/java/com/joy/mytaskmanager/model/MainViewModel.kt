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

    private val _navigateToDetail = MutableStateFlow<Task?>(null)
    val navigateToDetail: StateFlow<Task?> = _navigateToDetail.asStateFlow()

    fun selectTask(taskId: Int) {
        Log.i("MainViewModel", "selectTask() $taskId")
        viewModelScope.launch {
            _navigateToDetail.emit(tasks.value?.find { it.id == taskId })
        }
    }

    fun unselectCurrentTask() {
        Log.i("MainViewModel", "unselectCurrentTask()")
        viewModelScope.launch {
            _navigateToDetail.emit(null)
        }
    }

    // TODO: update and delete a task
}