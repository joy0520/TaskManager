package com.joy.mytaskmanager.data

import java.time.ZonedDateTime

class Task(
    val type: String,// TODO: create an enum class
    val description: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
}