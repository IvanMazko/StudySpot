package com.example.studyspot.data.local

import androidx.room.withTransaction
import com.example.studyspot.data.local.entity.SeatEntity
import com.example.studyspot.data.local.entity.TimeSlotEntity
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Initializes empty reference tables; existing data and bookings are left untouched. */
@Singleton
class DatabaseSeeder @Inject constructor(private val database: StudySpotDatabase) {

    private companion object {
        const val ROW_COUNT = 8
        const val COLUMN_COUNT = 7
    }

    /** Call and await before the repository first reads the hall data. */
    suspend fun seed() {
        database.withTransaction {
            val seatDao = database.seatDao()
            val timeSlotDao = database.timeSlotDao()

            if (seatDao.getAll().isEmpty()) {
                seatDao.insertAll(createSeats())
            }

            if (timeSlotDao.getAll().isEmpty()) {
                timeSlotDao.insertAll(createTimeSlots())
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
