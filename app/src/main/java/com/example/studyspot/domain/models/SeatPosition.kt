package com.example.studyspot.domain.models

/** Zero-based coordinates of a seat on the hall plan. */
data class SeatPosition(
    val row: Int,
    val column: Int,
) {
    init {
        require(row >= 0 && column >= 0) {
            "Seat coordinates must be non-negative"
        }
    }
}
