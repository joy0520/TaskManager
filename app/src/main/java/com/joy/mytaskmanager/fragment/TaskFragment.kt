package com.joy.mytaskmanager.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.fragment.placeholder.FakeTasksContent
import kotlinx.coroutines.launch

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
        Log.i("TaskFragment", "onCreateView()")

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyTaskRecyclerViewAdapter(FakeTasksContent.ITEMS) { taskId: Int ->
                    viewModel.selectTask(taskId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            Log.i("TaskFragment", "lifecycleScope.launch")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.i("TaskFragment", "repeatOnLifecycle(Lifecycle.State.STARTED)")
                viewModel.navigateToDetail.collect { task ->
                    Log.i("TaskFragment", "collected $task")

                    task?.also {
                        when (resources.configuration.orientation) {
                            Configuration.ORIENTATION_PORTRAIT -> {  // replace with DetailFragment
                                Log.i("TaskFragment", "portrait")

                                requireActivity().supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.task_container, DetailFragment.newInstance())
                                    .addToBackStack(null)
                                    .commit()
                            }

                            Configuration.ORIENTATION_LANDSCAPE -> {  // find DetailFragment and update its content
                                Log.i("TaskFragment", "landscape")

                                val detailFragment = requireActivity().supportFragmentManager
                                    .findFragmentByTag("DetailFragment") as? DetailFragment
                                detailFragment?.updateTaskDetail(it)
                            }

                            else -> {}
                        }
                    }
                }
            }

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("TaskFragment", "onViewCreated()")
    }

    override fun onStop() {
        super.onStop()
        Log.i("TaskFragment", "onStop()")
    }
}