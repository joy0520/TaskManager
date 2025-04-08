package com.joy.mytaskmanager.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
fun ZonedDateTime.toDt(): String = this.format(formatter)
fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, formatter)