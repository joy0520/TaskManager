package com.joy.mytaskmanager.data

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeConverter {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun fromZonedDateTime(zonedDateTime: ZonedDateTime?): String? {
        return zonedDateTime?.format(formatter)
    }

    @TypeConverter
    fun toZonedDateTime(zonedDateTimeString: String?): ZonedDateTime? {
        return zonedDateTimeString?.let { ZonedDateTime.parse(it, formatter) }
    }
}