package com.joy.mytaskmanager.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.model.MainViewModel
import com.joy.mytaskmanager.util.toDt
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {
    private val tag = "DetailFragment-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()
    private var currentTask: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(tag, "onCreateView()")

        // observe viewModel.selectedTask
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedTask.collect { task ->  // task could be null
                    updateTaskDetail(task)  // updateTaskDetail() can handle null task
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.i(tag, "handleOnBackPressed()")

                    if (viewModel.selectedTask.value != null) {
                        Log.i(tag, "viewModel.unselectCurrentTask() + popBackStack()")
                        viewModel.unselectCurrentTask()
                        // UI would observe null and become blank
                    } else {
                        // finish the app when selectedTask is null at landscape
                        requireActivity().finish()
                    }
                }
            })

        return inflater.inflate(R.layout.detail_fragment, container, false)
    }

    private fun updateTaskDetail(task: Task?) {
        Log.i(tag, "updateTaskDetail() $task")
        currentTask = task
        updateUiWithTask()
    }

    private fun updateUiWithTask() {
        view?.let {
            val taskTypeText: TextView = it.findViewById(R.id.task_type)
            val taskDescriptionText: TextView = it.findViewById(R.id.task_description)
            val startDateTime: TextView = it.findViewById(R.id.start_date_time)
            val endDateTime: TextView = it.findViewById(R.id.end_date_time)

            currentTask?.let { task ->
                taskTypeText.text = task.type.name
                taskDescriptionText.text = task.description
                startDateTime.text = task.start.toDt()
                endDateTime.text = task.end.toDt()
            } ?: run {   // clear the content
                taskTypeText.text = ""
                taskDescriptionText.text = ""
                startDateTime.text = ""
                endDateTime.text = ""
            }
        }
    }

    private fun isPortrait(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}