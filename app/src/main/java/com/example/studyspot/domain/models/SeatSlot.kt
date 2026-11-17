package com.example.studyspot.domain.models

import java.time.LocalDate

/** Availability of a seat for a date and a fixed time slot in the library's time zone. */
data class SeatSlot(
    val seat: Seat,
    val date: LocalDate,
    val timeSlot: TimeSlot,
    val status: AvailabilityStatus,
)
