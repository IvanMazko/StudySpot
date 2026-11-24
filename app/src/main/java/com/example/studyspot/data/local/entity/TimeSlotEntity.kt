package com.example.studyspot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.studyspot.data.local.RoomConverters
import java.time.LocalTime

@Entity(tableName = "time_slots")
@TypeConverters(RoomConverters::class)
data class TimeSlotEntity(
    @PrimaryKey val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
)
