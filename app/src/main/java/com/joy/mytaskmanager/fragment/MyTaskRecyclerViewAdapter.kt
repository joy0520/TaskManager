package com.joy.mytaskmanager.fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joy.mytaskmanager.R
import com.joy.mytaskmanager.data.Task

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyTaskRecyclerViewAdapter(
    private val values: List<Task>,
    private val onItemClick: (Int) -> Unit  // less couple to the ViewModel
) : RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder>() {
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
                    val clickedTask = values[position]
                    onItemClick(clickedTask.id)
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
        val task = values[position]
        holder.taskTypeView.text = task.type
        holder.taskDescriptionView.text = task.description
    }

    override fun getItemCount(): Int = values.size
}