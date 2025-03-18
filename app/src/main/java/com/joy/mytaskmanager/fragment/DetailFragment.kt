package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.joy.mytaskmanager.R

class DetailFragment : Fragment() {
    companion object {
        fun newInstance() = DetailFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.detail_fragment, container, false)
    }

    // after onCreateView()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set texts
        val taskTypeText: TextView = view.findViewById(R.id.task_type)
        val taskDescriptionText: TextView = view.findViewById(R.id.task_description)
        viewModel.selected.value?.also {
            taskTypeText.text = it.type
            taskDescriptionText.text = it.description
        }
    }

    override fun onStop() {
        super.onStop()

        // clear selected when this fragment is stopped, usually it is after the back is pressed
        viewModel.selected.value = null
    }
}