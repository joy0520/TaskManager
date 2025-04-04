package com.joy.mytaskmanager

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.joy.mytaskmanager.db.TaskDb
import com.joy.mytaskmanager.fragment.TaskFragment
import com.joy.mytaskmanager.model.MainViewModel
import com.joy.mytaskmanager.model.TaskViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val TASK_FRAGMENT_TAG = "task_list_fragment"
    }

    private lateinit var navController: NavController

    //    private var navControllerList: NavController? = null
//    private var navControllerDetail: NavController? = null
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val taskDb by lazy { TaskDb.getDatabase(applicationContext) }
    private val taskDao by lazy { taskDb.taskDao() }
    internal val taskViewModelFactory by lazy { TaskViewModelFactory(taskDao) }

    private val viewModel: MainViewModel by viewModels { taskViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate() savedInstanceState=$savedInstanceState")
        setContentView(R.layout.activity_main)
        setupToolbar()
        initializeNavigationController()
        ensureTaskListFragmentExists(savedInstanceState)
        subscribe()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!::navController.isInitialized) return super.onSupportNavigateUp()
        if (!::appBarConfiguration.isInitialized)
            return navController.navigateUp() || super.onSupportNavigateUp()

        if (isLandscape()) {
            Log.i(TAG, "onSupportNavigateUp() isLandscape")
            val currentDestinationId = navController.currentDestination?.id
            return if (currentDestinationId == R.id.detailFragment) {
                Log.i(TAG, "currentDestinationId == detailFragment, selectedTask=${viewModel.selectedTask.value}")
                if (viewModel.selectedTask.value != null) {
                    viewModel.unselectCurrentTask()
                    true
                } else {  // null task -> finish the app
                    finish()
                    true
                }
            } else {
                Log.i(TAG, "currentDestinationId != detailFragment")
                navController.navigateUp() || super.onSupportNavigateUp()
            }
        }

        // let NavController handle up navigation
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // +++ setup +++
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    /**
     * Finds the appropriate NavHostFragment based on orientation and initializes
     * the NavController and ActionBar integration once the NavHostFragment's view is created.
     */
    private fun initializeNavigationController() {
        val navHostFragmentId = if (isLandscape()) {
            R.id.detail_container
        } else {
            R.id.fragment_container
        }

        // Use FragmentLifecycleCallbacks for robust NavController initialization.
        // This ensures the NavHostFragment's view is ready.
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                if (f.id == navHostFragmentId && f is NavHostFragment) {
                    Log.i(
                        TAG,
                        "NavHostFragment (ID: ${f.id}) view created. Initializing NavController..."
                    )
                    // NavController is ready, store it
                    navController = f.navController

                    // Now setup ActionBar
                    setupToolbarWithNavController()

                    // Handle the initial state for the detail pane in landscape
                    navigateToInitialDetailPaneIfNeeded(savedInstanceState)

                    // Unregister the callback as we've initialized the NavController
                    fm.unregisterFragmentLifecycleCallbacks(this)
                    Log.d(TAG, "NavController initialized and callback unregistered.")
                }
            }
        }, false) // 'false' means recursive scan is off, which is usually fine here.
    }

    /**
     * Configures the ActionBar with the NavController once it's initialized.
     */
    private fun setupToolbarWithNavController() {
        // Define top-level destinations (usually the start destination)
        // Adjust this set if your Up behavior needs customization
        val topLevelDestinations = setOf(R.id.taskFragment)
        appBarConfiguration = AppBarConfiguration(topLevelDestinations)

        // Setup ActionBar. Use ViewBinding if available: setupActionBarWithNavController(navController, appBarConfiguration)
        setupActionBarWithNavController(navController, appBarConfiguration)
        Log.d(TAG, "ActionBar setup with NavController completed.")
    }

    /**
     * In landscape mode, if the NavController's graph starts at TaskFragment,
     * navigate the detail pane to a default state (e.g., empty or default detail).
     * Should be called only after NavController is initialized.
     */
    private fun navigateToInitialDetailPaneIfNeeded(savedInstanceState: Bundle?) {
        if (isLandscape() && savedInstanceState == null) {
            if (!::navController.isInitialized) return

            if (navController.graph.nodes.isEmpty ||
                navController.currentDestination?.id != R.id.taskFragment
            ) return

            // navigate from TaskFragment to DetailFragment
            try {
                Log.i(TAG, "navigateToInitialDetailPaneIfNeeded() task->detail")
                navController.navigate(R.id.action_taskFragment_to_detailFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Initial landscape navigation failed: $e")
            }
        }
    }


    /**
     * Ensures the TaskFragment is present in the 'task_list_container'
     * when in landscape mode, adding it if necessary on first creation.
     */
    private fun ensureTaskListFragmentExists(savedInstanceState: Bundle?) {
        if (!isLandscape()) {
            Log.d(TAG, "Not landscape, TaskFragment is managed by NavHost.")
            return // Only manage manually in landscape
        }

        val containerId = R.id.task_list_container
        val fragmentContainer = findViewById<View>(containerId) // Check if container exists
        if (fragmentContainer == null) {
            Log.e(
                TAG,
                "ensureTaskListFragmentExists: R.id.task_list_container not found in the current layout!"
            )
            return
        }

        // Check if the fragment already exists (restored by FragmentManager or added previously)
        val existingFragment = supportFragmentManager.findFragmentByTag(TASK_FRAGMENT_TAG)

        if (existingFragment == null) {
            // Only add the fragment if it's the first time the Activity is created
            // (savedInstanceState == null) to prevent adding it again on recreation.
            if (savedInstanceState == null) {
                Log.i(
                    TAG,
                    "Landscape first creation: Adding TaskFragment to container $containerId."
                )
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(containerId, TaskFragment::class.java, null, TASK_FRAGMENT_TAG)
                }
            } else {
                Log.w(
                    TAG,
                    "Landscape recreation: TaskFragment with tag $TASK_FRAGMENT_TAG not found, FragmentManager might not have restored it."
                )
                // Consider if you *need* to force-add it here, but usually restoration should handle it.
            }
        } else {
            Log.d(TAG, "TaskFragment with tag $TASK_FRAGMENT_TAG already exists.")
        }
    }

    private fun subscribe() {
        // observe MainViewModel.selectedTask and execute FragmentTransaction
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedTask.collect { task ->
                    if (!isLandscape() || !::navController.isInitialized) return@collect
                    // observation in DetailFragment handles null task
                    if (task == null) return@collect

                    // check if we should navigate to DetailFragment
                    val currentDestinationId = navController.currentDestination?.id
                    if (currentDestinationId == R.id.taskFragment)
                        navigateToDetail()
                }
            }
        }
    }
    // --- setup ---

    private fun isPortrait(newConfig: Configuration? = null): Boolean =
        (newConfig?.orientation ?: resources.configuration.orientation) ==
                Configuration.ORIENTATION_PORTRAIT

    private fun isLandscape(newConfig: Configuration? = null): Boolean =
        (newConfig?.orientation ?: resources.configuration.orientation) ==
                Configuration.ORIENTATION_LANDSCAPE

    private fun navigateToDetail() {
        if (!::navController.isInitialized || !isLandscape()) return

        try {
            navController.navigate(R.id.detailFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to DetailFragment: $e")
        }
    }
}