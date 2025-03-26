package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.adapter.TaskAdapter
import com.joy.mytaskmanager.model.MainViewModel

/**
 * A fragment representing a list of Items.
 */
class TaskFragment : Fragment() {
    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TaskFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    private val tag = "TaskFragment-${hashCode()}"

    private var columnCount = 1
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.task_fragment, container, false)
        Log.i(tag, "onCreateView() navigateToDetail=${viewModel.navigateToDetail.value}")

        // Set the adapter
        val taskAdapter = TaskAdapter(emptyList()) { taskId: Int ->
            viewModel.selectTask(taskId)
        }

        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
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