package com.example.studyspot.domain.models

data class Seat(
    val id: String,
    val label: String,
    val type: SeatType,
    val position: SeatPosition,
)
