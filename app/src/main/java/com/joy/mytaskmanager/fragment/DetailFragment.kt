package com.joy.mytaskmanager.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {
    companion object {
        fun newInstance() = DetailFragment()
    }

    private val tag = "DetailFragment-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()
    private var currentTask: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(tag, "onCreateView()")

        return inflater.inflate(R.layout.detail_fragment, container, false)
    }

    // after onCreateView()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set texts
        val taskTypeText: TextView = view.findViewById(R.id.task_type)
        val taskDescriptionText: TextView = view.findViewById(R.id.task_description)
        viewModel.navigateToDetail.value?.also {
            taskTypeText.text = it.type
            taskDescriptionText.text = it.description
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop()")

        // clear selected when this fragment is stopped, usually it is after the back is pressed
        viewModel.unselectCurrentTask()
    }

    fun updateTaskDetail(task: Task) {
        currentTask = task
        updateUiWithTask()
    }

    private fun updateUiWithTask() {
        view?.let {
            val taskTypeText: TextView = it.findViewById(R.id.task_type)
            val taskDescriptionText: TextView = it.findViewById(R.id.task_description)
            currentTask?.let { task ->
                taskTypeText.text = task.type
                taskDescriptionText.text = task.description
            } ?: run {   // clear the content
                taskTypeText.text = ""
                taskDescriptionText.text = ""
            }
        }
    }
}