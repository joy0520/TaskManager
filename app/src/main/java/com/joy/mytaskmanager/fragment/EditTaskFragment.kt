package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.data.TaskType
import com.joy.mytaskmanager.model.MainViewModel

class EditTaskFragment : Fragment() {
    private val tag = "AddTaskFragment-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_task_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup widgets
        viewModel.selectedTask.value?.also { task ->
            val typeSpinner = view.findViewById<Spinner>(R.id.spinner_type)
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                TaskType.values()
            )
            typeSpinner.adapter = adapter
            typeSpinner.setSelection(TaskType.values().indexOf(task.type))

            val descriptionEditor = view.findViewById<EditText>(R.id.edit_description)
            descriptionEditor.setText(task.description)

            val saveButton = view.findViewById<Button>(R.id.button_save)
            saveButton.setOnClickListener {
                val selectedType = typeSpinner.selectedItem as TaskType
                updateTask(
                    task.copy(
                        description = descriptionEditor.text.toString(),
                        type = selectedType
                    )
                )
                findNavController().popBackStack()
            }

            val cancelButton = view.findViewById<Button>(R.id.button_cancel)
            cancelButton.setOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun updateTask(task: Task) {
        viewModel.updateTask(task)
    }
}