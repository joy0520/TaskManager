package com.joy.mytaskmanager

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.joy.mytaskmanager.fragment.DetailFragment
import com.joy.mytaskmanager.fragment.MainViewModel
import com.joy.mytaskmanager.fragment.TaskFragment

class MainActivity : AppCompatActivity() {
    private val taskFragment: TaskFragment by lazy { TaskFragment.newInstance(1) }
    private val detailFragment: DetailFragment by lazy { DetailFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        Log.i("MainActivity", "navigateToDetail: ${viewModel.navigateToDetail.value}")

        // skip any Fragment addition to prevent duplicative addition
        if (savedInstanceState != null) return

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {  // add taskFragment only
                supportFragmentManager.beginTransaction()
                    .add(R.id.task_container, taskFragment, "TaskFragment")
                    .commit()
            }

            Configuration.ORIENTATION_LANDSCAPE -> {  // add both fragments
                supportFragmentManager.beginTransaction()
                    .add(R.id.task_list_container, taskFragment, "TaskFragment")
                    .add(R.id.detail_container, detailFragment, "DetailFragment")
                    .commit()
            }

            else -> {}  // nothing for now
        }
    }
}
// TODO: after rotation the fragments are not shown correctly