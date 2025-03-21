package com.joy.mytaskmanager.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.fragment.placeholder.FakeTasksContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var tasks: MutableLiveData<List<Task>> = MutableLiveData()

    private val _navigateToDetail = MutableStateFlow<Task?>(null)
    val navigateToDetail: StateFlow<Task?> = _navigateToDetail.asStateFlow()

    init {
        tasks.value = FakeTasksContent.ITEMS
    }

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
}