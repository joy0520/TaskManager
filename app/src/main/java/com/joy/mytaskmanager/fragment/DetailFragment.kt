package com.joy.mytaskmanager.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.model.MainViewModel
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {
    private val tag = "DetailFragment-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()
    private var currentTask: Task? = null

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_detail, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_edit -> {
                    Log.i(tag, "action_edit")
                    if (isPortrait()) findNavController().navigate(R.id.action_detailFragment_to_editTaskFragment)
                    else if (isLandscape()) findNavController().navigate(R.id.action_detailFragmentL_to_editTaskFragmentL)

                    true
                }

                R.id.action_cancel -> {
                    Log.i(tag, "action_cancel")
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(tag, "onCreateView()")

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedTask.collect { task ->
                    task?.also { updateTaskDetail(it) }
                }
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.i(tag, "handleOnBackPressed()")
                    viewModel.unselectCurrentTask()
                    parentFragmentManager.popBackStack()
                }
            })

        return inflater.inflate(R.layout.detail_fragment, container, false)
    }

    // after onCreateView()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(tag, "onViewCreated()")

        // set texts
        val taskTypeText: TextView = view.findViewById(R.id.task_type)
        val taskDescriptionText: TextView = view.findViewById(R.id.task_description)
        viewModel.selectedTask.value.also {
            Log.i(tag, "selectedTask.value: $it")
        }?.also {
            taskTypeText.text = it.type.name
            taskDescriptionText.text = it.description
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop()")
    }

    fun updateTaskDetail(task: Task) {
        Log.i(tag, "updateTaskDetail() $task")
        currentTask = task
        updateUiWithTask()
    }

    private fun updateUiWithTask() {
        view?.let {
            val taskTypeText: TextView = it.findViewById(R.id.task_type)
            val taskDescriptionText: TextView = it.findViewById(R.id.task_description)
            currentTask?.let { task ->
                taskTypeText.text = task.type.name
                taskDescriptionText.text = task.description
            } ?: run {   // clear the content
                taskTypeText.text = ""
                taskDescriptionText.text = ""
            }
        }
    }

    private fun isPortrait(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}