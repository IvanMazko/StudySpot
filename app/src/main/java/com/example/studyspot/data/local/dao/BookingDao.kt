package com.example.studyspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.example.studyspot.data.local.RoomConverters
import com.example.studyspot.data.local.entity.BookingEntity
import java.time.LocalDate

@Dao
@TypeConverters(RoomConverters::class)
interface BookingDao {

    @Insert
    suspend fun insert(booking: BookingEntity): Long

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getById(id: Long): BookingEntity?

    @Query("SELECT * FROM bookings WHERE date = :date AND timeSlotId = :timeSlotId ORDER BY seatId")
    suspend fun getForTimeSlot(date: LocalDate, timeSlotId: String): List<BookingEntity>

    /** Includes startDate, excludes endDateExclusive, matching the repository contract. */
    @Query(
        """
        SELECT * FROM bookings
        WHERE date >= :startDate AND date < :endDateExclusive
        ORDER BY date, timeSlotId, seatId
        """,
    )
    suspend fun getInDateRange(
        startDate: LocalDate,
        endDateExclusive: LocalDate,
    ): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY date, timeSlotId, seatId")
    suspend fun getForUser(userId: String): List<BookingEntity>

    /** Returns the number of deleted bookings: 0 if absent, 1 if deleted. */
    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
