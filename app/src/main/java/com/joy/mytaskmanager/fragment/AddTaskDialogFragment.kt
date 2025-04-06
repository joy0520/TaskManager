package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.data.TaskType
import com.joy.mytaskmanager.model.MainViewModel
import java.time.ZonedDateTime

class AddTaskDialogFragment : DialogFragment() {
    companion object {

    }

    private val tag = "${this.javaClass.simpleName}-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var spinnerType: Spinner
    private lateinit var editDescription: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_task_fragment, container, false)
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
            getString(R.string.add_fragment_title)

        spinnerType = view.findViewById(R.id.spinner_type)
        editDescription = view.findViewById(R.id.edit_description)
        buttonSave = view.findViewById(R.id.button_save)
        buttonCancel = view.findViewById(R.id.button_cancel)

        setupSpinner()
        buttonSave.setOnClickListener { saveTask() }
        buttonCancel.setOnClickListener { dismiss() }
    }

    private fun setupSpinner() {
        val taskTypeNames = TaskType.values().map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskTypeNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerType.adapter = adapter
    }

    private fun saveTask() {
        Log.i(tag, "saveTask()")
        val selectedTypeName = spinnerType.selectedItem as? String ?: return
        val description = editDescription.text.toString().trim()
        try {
            val selectedType = TaskType.valueOf(selectedTypeName)
            viewModel.addTask(
                Task(
                    type = selectedType,
                    description = description,
                    start = ZonedDateTime.now(),
                    end = ZonedDateTime.now().plusMinutes(20)
                )
            )

            Toast
                .makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT)
                .show()
            dismiss()
        } catch (e: IllegalArgumentException) {
            Toast
                .makeText(requireContext(), "Invalid task type selected", Toast.LENGTH_SHORT)
                .show()
            dismiss()
        } catch (e: Exception) {
            Log.e(tag, "Error adding a task: $e")
        }
    }
}