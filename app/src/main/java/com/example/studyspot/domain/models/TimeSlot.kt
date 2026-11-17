package com.example.studyspot.domain.models

import java.time.LocalTime

data class TimeSlot(
    val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
) {
    init {
        require(startTime < endTime) {
            "Time slot must end after it starts"
        }
    }
}
