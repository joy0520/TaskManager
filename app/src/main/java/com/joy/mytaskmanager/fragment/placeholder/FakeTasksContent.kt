package com.joy.mytaskmanager.fragment.placeholder

import com.joy.mytaskmanager.data.Task
import java.time.ZonedDateTime

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object FakeTasksContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<Task> = ArrayList()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createTask(i))
        }
    }

    private fun addItem(item: Task) {
        ITEMS.add(item)
    }

    private fun createTask(position: Int): Task {
        return Task(
            position,
            "fake",
            "position=$position",
            ZonedDateTime.now(),
            ZonedDateTime.now()
        )
    }
}