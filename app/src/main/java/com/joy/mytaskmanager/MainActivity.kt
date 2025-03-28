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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.joy.mytaskmanager.db.TaskDb
import com.joy.mytaskmanager.fragment.DetailFragment
import com.joy.mytaskmanager.model.MainViewModel
import com.joy.mytaskmanager.model.TaskViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var navControllerPortrait: NavController
    private lateinit var navControllerList: NavController
    private lateinit var navControllerDetail: NavController

    private val taskDb by lazy { TaskDb.getDatabase(applicationContext) }
    private val taskDao by lazy { taskDb.taskDao() }
    internal val taskViewModelFactory by lazy { TaskViewModelFactory(taskDao) }

    private val viewModel: MainViewModel by viewModels { taskViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupNavigation()
        subscribe()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "pressed $item")
        if (item.itemId == android.R.id.home) {
            return navControllerPortrait.navigateUp()  // up button on the left of toolbar
        }

        return super.onOptionsItemSelected(item)
    }

    // +++ setup +++
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun setupNavigation() {
        if (isPortrait()) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
            navControllerPortrait = navHostFragment.navController

            setupActionBarWithNavController(
                navControllerPortrait,
                AppBarConfiguration(navControllerPortrait.graph)
            )
        } else if (isLandscape()) {
            val navHostFragmentList = supportFragmentManager
                .findFragmentById(R.id.task_list_container) as NavHostFragment
            navControllerList = navHostFragmentList.navController

            val navHostFragmentDetail = supportFragmentManager
                .findFragmentById(R.id.detail_container) as NavHostFragment
            navControllerDetail = navHostFragmentDetail.navController

            setupActionBarWithNavController(
                navControllerList,
                AppBarConfiguration(navControllerList.graph)
            )
        }

    }

    private fun subscribe() {
        // observe MainViewModel.navigationToDetail and execute FragmentTransaction
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDetail.collect { task ->
                    if (task == null) {
                        // handle unselected if needed (e.g., navigate back)
                        if (isPortrait() && navControllerPortrait.currentDestination?.id == R.id.detailFragment)
                            navControllerPortrait.popBackStack()

                        return@collect
                    }

                    if (isPortrait()) {
                        // select a task -> go to detail fragment
                        navControllerPortrait.navigate(R.id.action_taskFragment_to_detailFragment)
                    } else if (isLandscape()) {
                        // For landscape, we might want to pass the task to the DetailFragment
                        // via arguments in the nav graph or update it directly if DetailFragmentL is held.
                        // If DetailFragmentL is managed by Navigation, consider using arguments.
                        // For simplicity, let's assume DetailFragmentL is still held and updated directly.
                        val detailFragment = supportFragmentManager
                            .findFragmentByTag("DetailFragmentL") as? DetailFragment
                        detailFragment?.updateTaskDetail(task)
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
}