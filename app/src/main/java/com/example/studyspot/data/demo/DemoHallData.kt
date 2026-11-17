package com.example.studyspot.data.demo

import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType

object DemoHallData {

    val seats: List<Seat> = listOf(
        Seat(
            id = "seat-1",
            label = "1",
            type = SeatType.REGULAR,
            position = SeatPosition(row = 0, column = 0),
        ),
        Seat(
            id = "seat-2",
            label = "2",
            type = SeatType.REGULAR,
            position = SeatPosition(row = 0, column = 1),
        ),
        Seat(
            id = "seat-3",
            label = "3",
            type = SeatType.COMPUTER,
            position = SeatPosition(row = 1, column = 0),
        ),
        Seat(
            id = "seat-4",
            label = "4",
            type = SeatType.COMPUTER,
            position = SeatPosition(row = 1, column = 1),
        ),
    )

    val occupiedSeatIds: Set<String> = setOf("seat-2")
}