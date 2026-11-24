package com.example.studyspot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.studyspot.data.local.dao.BookingDao
import com.example.studyspot.data.local.dao.SeatDao
import com.example.studyspot.data.local.dao.TimeSlotDao
import com.example.studyspot.data.local.entity.BookingEntity
import com.example.studyspot.data.local.entity.SeatEntity
import com.example.studyspot.data.local.entity.TimeSlotEntity

@Database(
    entities = [SeatEntity::class, TimeSlotEntity::class, BookingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StudySpotDatabase : RoomDatabase() {

    abstract fun seatDao(): SeatDao

    abstract fun timeSlotDao(): TimeSlotDao

    abstract fun bookingDao(): BookingDao
}
