package com.example.studyspot.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/** Stores local dates and times as ISO-8601 text without a time zone. */
class RoomConverters {

    @TypeConverter
    fun localDateToString(value: LocalDate): String = value.toString()

    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun localTimeToString(value: LocalTime): String = value.toString()

    @TypeConverter
    fun stringToLocalTime(value: String): LocalTime = LocalTime.parse(value)
}
