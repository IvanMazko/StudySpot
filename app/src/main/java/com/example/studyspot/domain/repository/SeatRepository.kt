package com.example.studyspot.domain.repository

import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.TimeSlot
import java.time.LocalDate

interface SeatRepository {

    suspend fun getSeatSlots(
        date: LocalDate,
        timeSlot: TimeSlot,
    ): List<SeatSlot>
}