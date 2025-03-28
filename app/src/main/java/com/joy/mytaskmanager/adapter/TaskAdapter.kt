package com.joy.mytaskmanager.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task

class TaskAdapter(
    private val onItemClick: (Int) -> Unit  // less couple to the ViewModel
) : ListAdapter<Task, TaskAdapter.ViewHolder>(TaskDiffCallback()) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTypeView: TextView = view.findViewById(R.id.task_type)
        val taskDescriptionView: TextView = view.findViewById(R.id.task_description)
        // TODO: task start and end time

        init {
            // set click listener
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                Log.i("ViewHolder", "item clicked: $position")
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { clickedTask ->
                        onItemClick(clickedTask.id)
                    }
                }
            }
        }

        override fun toString(): String {
            return "${super.toString()} '${taskTypeView.text}' '${taskDescriptionView.text}'"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { task ->
            holder.taskTypeView.text = task.type.name
            holder.taskDescriptionView.text = task.description
        }
    }
}