package com.example.studyspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.studyspot.data.local.entity.TimeSlotEntity

@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM time_slots ORDER BY startTime, id")
    suspend fun getAll(): List<TimeSlotEntity>

    @Query("SELECT * FROM time_slots WHERE id = :id")
    suspend fun getById(id: String): TimeSlotEntity?

    @Insert
    suspend fun insertAll(timeSlots: List<TimeSlotEntity>)
}
