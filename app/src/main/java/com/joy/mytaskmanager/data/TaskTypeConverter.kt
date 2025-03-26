package com.joy.mytaskmanager.data

import androidx.room.TypeConverter

class TaskTypeConverter {
    @TypeConverter
    fun fromTaskType(taskType: TaskType): String = taskType.name

    @TypeConverter
    fun toTaskType(taskTypeString: String): TaskType = TaskType.valueOf(taskTypeString)
}