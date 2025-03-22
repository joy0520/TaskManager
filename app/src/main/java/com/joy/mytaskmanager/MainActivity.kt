package com.joy.mytaskmanager

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.joy.mytaskmanager.fragment.DetailFragment
import com.joy.mytaskmanager.fragment.MainViewModel
import com.joy.mytaskmanager.fragment.TaskFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val taskFragmentP: TaskFragment by lazy { TaskFragment.newInstance(1) }
    private val taskFragmentL: TaskFragment by lazy { TaskFragment.newInstance(1) }
    private val detailFragmentP: DetailFragment by lazy { DetailFragment.newInstance() }
    private val detailFragmentL: DetailFragment by lazy { DetailFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        Log.i(TAG, "navigateToDetail: ${viewModel.navigateToDetail.value}")

        // skip any Fragment addition to prevent duplicative addition
//        if (savedInstanceState != null) return

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {  // add taskFragment only
                Log.i(TAG, "ORIENTATION_PORTRAIT")
                if (taskFragmentExistsPortrait()) return

                Log.i(TAG, "add TaskFragment P")
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, taskFragmentP, "TaskFragmentP")
                    .commit()
            }

            Configuration.ORIENTATION_LANDSCAPE -> {  // add both fragments
                Log.i(TAG, "ORIENTATION_LANDSCAPE")
                val transaction = supportFragmentManager.beginTransaction()
                if (!taskFragmentExistsLandscape()) {
                    Log.i(TAG, "add TaskFragment L")
                    transaction.add(R.id.task_list_container, taskFragmentL, "TaskFragmentL")
                }
                if (!detailFragmentExistsLandscape()) {
                    Log.i(TAG, "add DetailFragment L")
                    transaction.add(R.id.detail_container, detailFragmentL, "DetailFragmentL")
                }

                transaction.commit()
            }

            else -> {}  // nothing for now
        }

        // observe MainViewModel.navigationToDetail and execute FragmentTransaction
        lifecycleScope.launch {
            viewModel.navigateToDetail.collect { task ->
                if (task == null) return@collect
                if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) return@collect
                if (detailFragmentExistsPortrait()) return@collect

                Log.i(TAG, "navigateToDetail: $task")
                showDetailFragmentPortrait()
            }
        }
    }

    private fun taskFragmentExistsPortrait(): Boolean =
        supportFragmentManager.findFragmentByTag("TaskFragmentP") != null

    private fun taskFragmentExistsLandscape(): Boolean =
        supportFragmentManager.findFragmentByTag("TaskFragmentL") != null

    private fun detailFragmentExistsPortrait(): Boolean =
        supportFragmentManager.findFragmentByTag("DetailFragmentP") != null

    private fun detailFragmentExistsLandscape(): Boolean =
        supportFragmentManager.findFragmentByTag("DetailFragmentL") != null

    private fun showDetailFragmentPortrait() =
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragmentP, "DetailFragmentP")
            .addToBackStack(null)
            .commit()

    private fun showDetailFragmentLandscape() =
        supportFragmentManager.beginTransaction()
            .add(R.id.detail_container, detailFragmentL, "DetailFragmentL")
            .commit()

}
// TODO: after rotation the fragments are not shown correctly