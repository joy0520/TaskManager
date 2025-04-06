package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.model.MainViewModel

class DeleteTaskDialogFragment : DialogFragment() {
    private val tag = "${this.javaClass.simpleName}-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.delete_task_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.dialog_title)?.text =
            getString(R.string.delete_fragment_title)

        // setup textViews and buttons
        viewModel.selectedTask.value?.also {
            val taskTypeText: TextView = view.findViewById(R.id.task_type)
            taskTypeText.text = it.type.name

            val taskDescriptionText: TextView = view.findViewById(R.id.task_description)
            taskDescriptionText.text = it.description

            val saveButton = view.findViewById<Button>(R.id.button_save)
            saveButton.setOnClickListener { _ ->
                viewModel.deleteTask(it.id)
                Toast.makeText(
                    requireContext(),
                    "Task ${it.type.name} Deleted",
                    Toast.LENGTH_SHORT
                )
                    .show()
                dismiss()
            }

            val cancelButton = view.findViewById<Button>(R.id.button_cancel)
            cancelButton.setOnClickListener { dismiss() }
        }
    }
}