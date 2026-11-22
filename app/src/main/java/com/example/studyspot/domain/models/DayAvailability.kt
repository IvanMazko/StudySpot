package com.example.studyspot.domain.models

import java.time.LocalDate

data class DayAvailability(
    val date: LocalDate,
    val availableTimeSlots: List<TimeSlot>,
)
