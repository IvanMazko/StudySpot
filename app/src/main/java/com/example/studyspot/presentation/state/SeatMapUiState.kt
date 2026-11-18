package com.example.studyspot.presentation.state

import com.example.studyspot.domain.models.SeatSlot

data class SeatMapUiState(
    val seatSlots: List<SeatSlot> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
