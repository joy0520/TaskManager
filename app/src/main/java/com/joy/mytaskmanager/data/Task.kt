package com.joy.mytaskmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo
    val type: TaskType,
    @ColumnInfo
    val description: String,
    @ColumnInfo
    val start: ZonedDateTime,
    @ColumnInfo
    val end: ZonedDateTime,
) {
    override fun toString(): String =
        "id=$id $type $description"
}