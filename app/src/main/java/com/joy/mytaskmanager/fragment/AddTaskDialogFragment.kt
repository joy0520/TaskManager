package com.joy.mytaskmanager.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.data.TaskType
import com.joy.mytaskmanager.model.MainViewModel
import com.joy.mytaskmanager.util.toDt
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class AddTaskDialogFragment : DialogFragment() {
    companion object {

    }

    private val tag = "${this.javaClass.simpleName}-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var spinnerType: Spinner
    private lateinit var editDescription: EditText
    private lateinit var startDateTime: TextView
    private lateinit var buttonStartDateTimePicker: ImageButton
    private lateinit var endDateTime: TextView
    private lateinit var buttonEndDateTimePicker: ImageButton
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    private var selectedStartDateTime: ZonedDateTime? = null
    private var selectedEndDateTime: ZonedDateTime? = null

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

        findViews(view)
        setupSpinner()
        setupButtons()

        updateDateTimeDisplay()
    }

    private fun findViews(view: View) {
        spinnerType = view.findViewById(R.id.spinner_type)
        editDescription = view.findViewById(R.id.edit_description)
        startDateTime = view.findViewById(R.id.start_date_time)
        buttonStartDateTimePicker = view.findViewById(R.id.start_date_time_picker_button)
        endDateTime = view.findViewById(R.id.end_date_time)
        buttonEndDateTimePicker = view.findViewById(R.id.end_date_time_picker_button)
        buttonSave = view.findViewById(R.id.button_save)
        buttonCancel = view.findViewById(R.id.button_cancel)
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

    private fun setupButtons() {
        buttonStartDateTimePicker.setOnClickListener { showDatePickerDialog(true) }
        buttonEndDateTimePicker.setOnClickListener { showDatePickerDialog(false) }

        buttonSave.setOnClickListener { if (isValidDateTime()) saveTask() }
        buttonCancel.setOnClickListener { dismiss() }
    }

    private fun showDatePickerDialog(isStart: Boolean) {
        val defaultSelection = (if (isStart) selectedStartDateTime else selectedEndDateTime)
            ?.toInstant()?.toEpochMilli()
            ?: MaterialDatePicker.todayInUtcMilliseconds()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.select_date)
            .setSelection(defaultSelection)
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
            // choose time then
            showTimePickerDialog(isStart, selectedDateMillis)
        }

        datePicker.show(
            parentFragmentManager,
            if (isStart) "START_DATE_PICKER" else "END_DATE_PICKER"
        )
    }

    private fun showTimePickerDialog(isStart: Boolean, selectedDateMillis: Long) {
        val defaultDt =
            (if (isStart) selectedStartDateTime else selectedEndDateTime) ?: ZonedDateTime.now()

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText(R.string.select_time)
            .setHour(defaultDt.hour)
            .setMinute(defaultDt.minute)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val selectedInstant = Instant.ofEpochMilli(selectedDateMillis)
            val selectedZonedDt = selectedInstant.atZone(ZoneId.systemDefault())
                .withHour(timePicker.hour)
                .withMinute(timePicker.minute)

            if (isStart) selectedStartDateTime = selectedZonedDt
            else selectedEndDateTime = selectedZonedDt

            updateDateTimeDisplay()
        }

        timePicker.show(
            parentFragmentManager,
            if (isStart) "START_TIME_PICKER" else "END_TIME_PICKER"
        )
    }

    private fun updateDateTimeDisplay() {
        startDateTime.text =
            selectedStartDateTime?.toDt() ?: getString(R.string.hint_select_date_time)
        endDateTime.text =
            selectedEndDateTime?.toDt() ?: getString(R.string.hint_select_date_time)
    }

    private fun isValidDateTime(): Boolean {
        when {
            selectedStartDateTime == null && selectedEndDateTime == null -> R.string.toast_pick_start_and_end
            selectedStartDateTime == null -> R.string.toast_pick_start
            selectedEndDateTime == null -> R.string.toast_pick_end
            else -> null
        }?.also {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        return selectedStartDateTime != null && selectedEndDateTime != null
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
                    start = selectedStartDateTime!!,
                    end = selectedEndDateTime!!,
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