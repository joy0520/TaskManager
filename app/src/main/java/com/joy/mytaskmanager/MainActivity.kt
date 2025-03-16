package com.joy.mytaskmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joy.mytaskmanager.fragment.TaskFragment

class MainActivity : AppCompatActivity() {
    private val taskFragment: TaskFragment by lazy { TaskFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .add(
                R.id.task_recycler,
                taskFragment,
                "TaskFragment"
            ).commit()  // TODO: add another show-content fragment
    }
}