package com.example.studyspot.data.demo

import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

object DemoHallData {

    private const val ROW_COUNT = 8
    private const val COLUMN_COUNT = 7

    val seats: List<Seat> = createSeats()

    val timeSlots: List<TimeSlot> = listOf(
        TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0)),
        TimeSlot("afternoon", LocalTime.of(12, 0), LocalTime.of(15, 0)),
        TimeSlot("evening", LocalTime.of(15, 0), LocalTime.of(18, 0)),
    )

    private val morningOccupiedSeatIds: Set<String> = setOf("seat-2", "seat-11", "seat-14", "seat-23", "seat-29", "seat-35", "seat-37","seat-39",
        "seat-46", "seat-50", "seat-52", "seat-54")

    // Dates are anchored once when the demo source is first initialized.
    val demoStartDate: LocalDate = LocalDate.now()

    val occupiedSeatSlots: List<SeatSlot> = createOccupiedSeatSlots()

    private fun createOccupiedSeatSlots(): List<SeatSlot> {
        val result = mutableListOf<SeatSlot>()

        for (dayOffset in 0..3) {
            val date = demoStartDate.plusDays(dayOffset.toLong())

            for (timeSlot in timeSlots) {
                for (seat in seats) {
                    val occupied = when (dayOffset) {
                        // Today: the original occupied seats, but only in the morning.
                        0 -> timeSlot.id == "morning" && seat.id in morningOccupiedSeatIds
                        // Tomorrow: computers are occupied all day; regular seats are free.
                        1 -> seat.type == SeatType.COMPUTER
                        // In two days: the afternoon is fully occupied.
                        2 -> timeSlot.id == "afternoon"
                        // In three days: no seats are available in any interval.
                        3 -> true
                        else -> false
                    }

                    if (occupied) {
                        result.add(
                            SeatSlot(
                                seat = seat,
                                date = date,
                                timeSlot = timeSlot,
                                status = AvailabilityStatus.OCCUPIED,
                            ),
                        )
                    }
                }
            }
        }

        return result
    }

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
