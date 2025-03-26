package com.joy.mytaskmanager.adapter

import androidx.recyclerview.widget.DiffUtil
import com.joy.mytaskmanager.data.Task

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}