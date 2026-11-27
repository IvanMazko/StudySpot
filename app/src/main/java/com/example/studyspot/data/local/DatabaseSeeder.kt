package com.example.studyspot.data.local

import androidx.room.withTransaction
import com.example.studyspot.data.local.entity.SeatEntity
import com.example.studyspot.data.local.entity.BookingEntity
import com.example.studyspot.data.local.entity.TimeSlotEntity
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType
import java.time.LocalTime
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Seeds a new hall and demo bookings atomically; existing bookings are never recreated. */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: StudySpotDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    private companion object {
        const val ROW_COUNT = 8
        const val COLUMN_COUNT = 7
    }

    /** Call and await before the repository first reads the hall data. */
    suspend fun seed() {
        database.withTransaction {
            val seatDao = database.seatDao()
            val timeSlotDao = database.timeSlotDao()
            val seatsEmpty = seatDao.getAll().isEmpty()
            val slotsEmpty = timeSlotDao.getAll().isEmpty()

            if (seatsEmpty) {
                seatDao.insertAll(createSeats())
            }

            if (slotsEmpty) {
                timeSlotDao.insertAll(createTimeSlots())
            }
            if (seatsEmpty && slotsEmpty) {
                seedDemoBookings(seatDao.getAll(), timeSlotDao.getAll())
            }
        }
    }

    private suspend fun seedDemoBookings(seats: List<SeatEntity>, slots: List<TimeSlotEntity>) {
        val today = LocalDate.now(clock)
        val morningSeats = setOf(
            "seat-2", "seat-11", "seat-14", "seat-23", "seat-29", "seat-35",
            "seat-37", "seat-39", "seat-46", "seat-50", "seat-52", "seat-54",
        )
        for (dayOffset in 0..3) {
            for (slot in slots) {
                for (seat in seats) {
                    val occupied = when (dayOffset) {
                        0 -> slot.id == "morning" && seat.id in morningSeats
                        1 -> seat.type == SeatType.COMPUTER
                        2 -> slot.id == "afternoon"
                        else -> true
                    }
                    if (occupied) {
                        database.bookingDao().insert(
                            BookingEntity(
                                seatId = seat.id,
                                date = today.plusDays(dayOffset.toLong()),
                                timeSlotId = slot.id,
                                userId = "demo-user",
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun createSeats(): List<SeatEntity> {
        // Preserve the current hall layout and repeatable computer-seat distribution.
        val random = Random(seed = 42)
        return buildList {
            for (row in 0 until ROW_COUNT) {
                for (column in 0 until COLUMN_COUNT) {
                    val number = row * COLUMN_COUNT + column + 1
                    add(
                        SeatEntity(
                            id = "seat-$number",
                            label = number.toString(),
                            type = if (random.nextInt(4) == 0) {
                                SeatType.COMPUTER
                            } else {
                                SeatType.REGULAR
                            },
                            position = SeatPosition(row = row, column = column),
                        ),
                    )
                }
            }
        }
    }

    private fun createTimeSlots(): List<TimeSlotEntity> = listOf(
        TimeSlotEntity("morning", LocalTime.of(9, 0), LocalTime.of(12, 0)),
        TimeSlotEntity("afternoon", LocalTime.of(12, 0), LocalTime.of(15, 0)),
        TimeSlotEntity("evening", LocalTime.of(15, 0), LocalTime.of(18, 0)),
    )


}
