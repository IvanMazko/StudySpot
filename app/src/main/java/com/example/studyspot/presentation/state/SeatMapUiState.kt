package com.example.studyspot.presentation.state

import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.DayAvailability
import com.example.studyspot.domain.models.TimeSlot
import java.time.LocalDate

data class SeatMapUiState(
    val seatSlots: List<SeatSlot> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSeatId: String? = null,
    val requiresComputer: Boolean = false,
    val timeSlots: List<TimeSlot> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedTimeSlot: TimeSlot? = null,
    val availableDays: List<DayAvailability> = emptyList(),
    val isAvailabilityLoading: Boolean = false,
    val availabilityErrorMessage: String? = null,
)
