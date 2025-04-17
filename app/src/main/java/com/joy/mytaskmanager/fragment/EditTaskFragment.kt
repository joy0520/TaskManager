package com.joy.mytaskmanager.fragment

import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
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

class EditTaskFragment : Fragment() {
    private val tag = "AddTaskFragment-${hashCode()}"
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var typeSpinner: Spinner
    private lateinit var descriptionEditor: EditText
    private lateinit var startDateTime: TextView
    private lateinit var buttonStartDateTimePicker: ImageButton
    private lateinit var endDateTime: TextView
    private lateinit var buttonEndDateTimePicker: ImageButton
    private lateinit var switchNotificationEnabled: MaterialSwitch
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var selectedStartDateTime: ZonedDateTime? = null
    private var selectedEndDateTime: ZonedDateTime? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_task_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        // setup widgets
        viewModel.selectedTask.value?.also { task ->
            selectedStartDateTime = task.start
            selectedEndDateTime = task.end

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                TaskType.values()
            )
            typeSpinner.adapter = adapter
            typeSpinner.setSelection(TaskType.values().indexOf(task.type))

            descriptionEditor.setText(task.description)

            setupButtons(task)
            setupSwitches(task)
            updateDateTimeDisplay(task)
        }
    }

    private fun initViews(view: View) {
        typeSpinner = view.findViewById(R.id.spinner_type)
        descriptionEditor = view.findViewById(R.id.edit_description)
        startDateTime = view.findViewById(R.id.start_date_time)
        buttonStartDateTimePicker = view.findViewById(R.id.start_date_time_picker_button)
        endDateTime = view.findViewById(R.id.end_date_time)
        buttonEndDateTimePicker = view.findViewById(R.id.end_date_time_picker_button)
        switchNotificationEnabled = view.findViewById(R.id.switch_notification_enable)
        saveButton = view.findViewById(R.id.button_save)
        cancelButton = view.findViewById(R.id.button_cancel)
    }

    private fun setupButtons(task: Task) {
        buttonStartDateTimePicker.setOnClickListener { showDatePickerDialog(true) }
        buttonEndDateTimePicker.setOnClickListener { showDatePickerDialog(false) }
        saveButton.setOnClickListener { if (isValidDateTime()) saveTask(task) }
        cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupSwitches(task: Task) {
        switchNotificationEnabled.isChecked = task.isNotificationEnabled
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
        val defaultDt = (if (isStart) selectedStartDateTime else selectedEndDateTime)
            ?: ZonedDateTime.now()

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

    private fun updateDateTimeDisplay(task: Task? = null) {
        startDateTime.text = task?.start?.toDt()
            ?: selectedStartDateTime?.toDt()
                    ?: getString(R.string.hint_select_date_time)
        endDateTime.text = task?.end?.toDt()
            ?: selectedEndDateTime?.toDt()
                    ?: getString(R.string.hint_select_date_time)
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

    private fun saveTask(originalTask: Task) {
        val selectedType = typeSpinner.selectedItem as TaskType
        viewModel.updateTask(
            originalTask.copy(
                description = descriptionEditor.text.toString(),
                type = selectedType,
                start = selectedStartDateTime!!,
                end = selectedEndDateTime!!,
                isNotificationEnabled = switchNotificationEnabled.isChecked
            )
        )
        findNavController().popBackStack()
    }
}