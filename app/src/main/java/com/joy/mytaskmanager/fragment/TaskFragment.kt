package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joy.mytaskmanager.MainActivity
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.adapter.TaskAdapter
import com.joy.mytaskmanager.model.MainViewModel

/**
 * A fragment representing a list of Items.
 */
class TaskFragment : Fragment() {
    private val tag = "TaskFragment-${hashCode()}"

    private val viewModel: MainViewModel by activityViewModels {
        (requireActivity() as MainActivity).taskViewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_fragment, container, false)
        Log.i(tag, "onCreateView() navigateToDetail=${viewModel.navigateToDetail.value}")

        // Set the adapter
        val taskAdapter = TaskAdapter { taskId: Int ->
            viewModel.selectTask(taskId)
        }

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = taskAdapter
            }
        }

        // observe viewModel.tasks and update tasks in adapter
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            Log.i(tag, "observing allTasks: $tasks")
            taskAdapter.submitList(tasks)
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop()")
    }
}