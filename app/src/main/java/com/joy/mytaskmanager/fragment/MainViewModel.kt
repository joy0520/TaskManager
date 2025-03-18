package com.joy.mytaskmanager.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.fragment.placeholder.FakeTasksContent

class MainViewModel : ViewModel() {
    var tasks: MutableLiveData<List<Task>> = MutableLiveData()
    var selected: MutableLiveData<Task> = MutableLiveData()

    init {
        tasks.value = FakeTasksContent.ITEMS
    }

    fun openTask(task: Task) {
        selected.value = task
    }
}