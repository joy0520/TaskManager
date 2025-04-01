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
import com.joy.mytaskmanager.fragment.DetailFragment
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "pressed $item")
        if (item.itemId == android.R.id.home) {
            return navController.navigateUp()  // up button on the left of toolbar
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
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
            // Check if NavController is initialized AND current destination is the list fragment
            if (::navController.isInitialized && navController.currentDestination?.id == R.id.taskFragment) {
                Log.i(
                    TAG,
                    "Landscape initial load: Detail pane needs navigation from TaskFragment start destination."
                )
                try {
                    navController.navigate(R.id.action_taskFragment_to_detailFragment)
                    Log.d(TAG, "Navigated detail pane to initial DetailFragment state.")
                } catch (e: IllegalStateException) {
                    Log.e(
                        TAG,
                        "Safe Args NavDirections not found. Ensure graph and actions are correct.",
                        e
                    )
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Navigation failed, likely destination not found from current.", e)
                }
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

//    private fun setupNavigation(savedInstanceState: Bundle?) {
//        if (isPortrait()) {
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
//            navController = navHostFragment.navController
//
//            setupActionBarWithNavController(
//                navController,
//                AppBarConfiguration(navController.graph)
//            )
//        } else if (isLandscape()) {
//            // landscape + first time Activity onCreate -> add TaskFragment
//            supportFragmentManager.commit {
//                add(R.id.task_list_container, TaskFragment::class.java, null)
//            }
//
//            val navHostFragmentDetail = supportFragmentManager
//                .findFragmentById(R.id.detail_container) as NavHostFragment
//            navController = navHostFragmentDetail.navController
//
//            setupActionBarWithNavController(
//                navController,
//                AppBarConfiguration(navController.graph)
//            )
//        }
//
//    }

    private fun subscribe() {
        // observe MainViewModel.navigationToDetail and execute FragmentTransaction
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedTask.collect { task ->
                    if (!isLandscape()) return@collect
                    if (task == null) return@collect

                    val detailFragment = supportFragmentManager
                        .findFragmentByTag("DetailFragmentL") as? DetailFragment
                    detailFragment?.updateTaskDetail(task)
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
}