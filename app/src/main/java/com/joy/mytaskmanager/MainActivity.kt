package com.joy.mytaskmanager

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.joy.mytaskmanager.db.TaskDb
import com.joy.mytaskmanager.fragment.DetailFragment
import com.joy.mytaskmanager.model.MainViewModel
import com.joy.mytaskmanager.fragment.TaskFragment
import com.joy.mytaskmanager.model.TaskViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val taskFragmentP: TaskFragment by lazy { TaskFragment.newInstance(1) }
    private val taskFragmentL: TaskFragment by lazy { TaskFragment.newInstance(1) }
    private val detailFragmentP: DetailFragment by lazy { DetailFragment.newInstance() }
    private val detailFragmentL: DetailFragment by lazy { DetailFragment.newInstance() }

    private val taskDb by lazy { TaskDb.getDatabase(applicationContext) }
    private val taskDao by lazy { taskDb.taskDao() }
    private val taskViewModelFactory by lazy { TaskViewModelFactory(taskDao) }
//    private val viewModel: MainViewModel by activityViewModels { taskViewModelFactory }
    private val viewModel: MainViewModel by viewModels { taskViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
        setupToolbar()
        initViews()
        subscribe()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "pressed $item")
        if (item.itemId == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // +++ setup +++
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun initViews() {
        Log.i(TAG, "navigateToDetail: ${viewModel.navigateToDetail.value}")

        when {
            isPortrait() -> {  // add taskFragment only
                Log.i(TAG, "ORIENTATION_PORTRAIT")
                if (!taskFragmentExistsPortrait()) {
                    Log.i(TAG, "add TaskFragment P")
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, taskFragmentP, "TaskFragmentP")
                        .commit()
                }

                viewModel.navigateToDetail.value?.also { task ->
                    showDetailFragmentPortrait()
                    detailFragmentP.updateTaskDetail(task)
                }
            }

            isLandscape() -> {  // add both fragments
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
    }

    private fun subscribe() {
        // observe MainViewModel.navigationToDetail and execute FragmentTransaction
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDetail.collect { task ->
                    if (task == null) return@collect

                    if (isPortrait()) {
                        if (detailFragmentExistsPortrait()) return@collect

                        Log.i(TAG, "portrait collect{} $task")
                        showDetailFragmentPortrait()
                    } else if (isLandscape()) {
                        if (detailFragmentExistsLandscape()) return@collect

                        Log.i(TAG, "landscape collect{} $task")
                        detailFragmentL.updateTaskDetail(task)
                    }
                }
            }
        }
    }
    // --- setup ---

    private fun isPortrait(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun taskFragmentExistsPortrait(): Boolean =
        supportFragmentManager.findFragmentByTag("TaskFragmentP") != null

    private fun taskFragmentExistsLandscape(): Boolean =
        supportFragmentManager.findFragmentByTag("TaskFragmentL") != null

    private fun detailFragmentExistsPortrait(): Boolean =
        supportFragmentManager.findFragmentByTag("DetailFragmentP") != null

    private fun detailFragmentExistsLandscape(): Boolean =
        supportFragmentManager.findFragmentByTag("DetailFragmentL") != null

    private fun showDetailFragmentPortrait() {
        Log.i(TAG, "showDetailFragmentPortrait()")
        val transaction = supportFragmentManager.beginTransaction()
        if (detailFragmentExistsPortrait()) {
            Log.i(TAG, "detailFragmentExistsPortrait")
            supportFragmentManager.findFragmentByTag("DetailFragmentP")?.also {
                Log.i(TAG, "transaction remove $it")
                transaction.remove(it)
            }
        }

        transaction
            .replace(R.id.fragment_container, detailFragmentP, "DetailFragmentP")
            .addToBackStack(null)
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}