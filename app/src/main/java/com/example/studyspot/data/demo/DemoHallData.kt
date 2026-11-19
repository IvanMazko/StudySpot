package com.example.studyspot.data.demo

import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType
import kotlin.random.Random

object DemoHallData {

    private const val ROW_COUNT = 8
    private const val COLUMN_COUNT = 7

    val seats: List<Seat> = createSeats()

    val occupiedSeatIds: Set<String> = setOf("seat-2", "seat-11", "seat-14", "seat-23", "seat-29", "seat-35", "seat-37","seat-39",
        "seat-46", "seat-50", "seat-52", "seat-54")

    private fun createSeats(): List<Seat> {
        val result = mutableListOf<Seat>()
        // Repeatable computer-seat distribution; coordinates are not random.
        val random = Random(seed = 42)
        var seatNumber = 1

        for (row in 0 until ROW_COUNT) {
            for (column in 0 until COLUMN_COUNT) {
                val type = if (random.nextInt(4) == 0) {
                    SeatType.COMPUTER
                } else {
                    SeatType.REGULAR
                }

                val seat = Seat(
                    id = "seat-$seatNumber",
                    label = seatNumber.toString(),
                    type = type,
                    position = SeatPosition(
                        row = row,
                        column = column,
                    ),
                )

                result.add(seat)
                seatNumber++
            }
        }

        return result
    }
}
