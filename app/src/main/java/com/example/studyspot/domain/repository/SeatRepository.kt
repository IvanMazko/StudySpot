package com.example.studyspot.domain.repository

import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.DayAvailability
import com.example.studyspot.domain.models.TimeSlot
import java.time.LocalDate

interface SeatRepository {
    suspend fun getTimeSlots(): List<TimeSlot>

    /** Includes startDate, excludes endDateExclusive. Empty intervals mean an unavailable day. */
    suspend fun getAvailability(
        startDate: LocalDate,
        endDateExclusive: LocalDate,
        requiresComputer: Boolean,
    ): List<DayAvailability>

    suspend fun getSeatSlots(
        date: LocalDate,
        timeSlot: TimeSlot,
    ): List<SeatSlot>
}
