package com.joy.mytaskmanager.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.joy.mytaskmanager.dao.TaskDao
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.data.TaskTypeConverter
import com.joy.mytaskmanager.data.ZonedDateTimeConverter
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile

@Database(entities = [Task::class], version = 1)
@TypeConverters(TaskTypeConverter::class, ZonedDateTimeConverter::class)
abstract class TaskDb : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: TaskDb? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): TaskDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDb::class.java,
                    "task_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                return instance
            }
        }
    }

    abstract fun taskDao(): TaskDao
}