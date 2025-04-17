package com.joy.mytaskmanager.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.joy.mytaskmanager.dao.TaskDao
import com.joy.mytaskmanager.data.Task
import com.joy.mytaskmanager.data.TaskTypeConverter
import com.joy.mytaskmanager.data.ZonedDateTimeConverter
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile

@Database(entities = [Task::class], version = 2)
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
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance

                return instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task ADD COLUMN isNotificationEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    abstract fun taskDao(): TaskDao
}