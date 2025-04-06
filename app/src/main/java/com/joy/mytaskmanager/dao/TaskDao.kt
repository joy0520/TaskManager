package com.joy.mytaskmanager.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.joy.mytaskmanager.data.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task WHERE id = :taskId")
    suspend fun delete(taskId: Int)

    @Query("SELECT * from task WHERE id= :id")
    suspend fun task(id: Int): Task

    @Query("SELECT * from task ORDER BY id ASC")
    fun allTasks(): LiveData<List<Task>>
}