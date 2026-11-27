package com.example.studyspot.data.repository

import androidx.room.withTransaction
import com.example.studyspot.data.local.StudySpotDatabase
import com.example.studyspot.data.local.entity.BookingEntity
import com.example.studyspot.data.local.entity.SeatEntity
import com.example.studyspot.data.local.entity.TimeSlotEntity
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.DayAvailability
import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRoomRepository @Inject constructor(
    private val database: StudySpotDatabase,
) : SeatRepository {

    override suspend fun getTimeSlots(): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        for (slot in database.timeSlotDao().getAll()) {
            slots.add(slot.toModel())
        }
        return slots
    }

    override suspend fun getAvailability(
        startDate: LocalDate,
        endDateExclusive: LocalDate,
        requiresComputer: Boolean,
    ): List<DayAvailability> {
        require(endDateExclusive.isAfter(startDate)) {
            "Конец диапазона должен быть позже начала: $startDate — $endDateExclusive"
        }
        val seats = getSuitableSeats(requiresComputer)
        val slots = getTimeSlots()
        val bookings = database.bookingDao().getInDateRange(startDate, endDateExclusive)
        val occupiedSeatsBySlot = getOccupiedSeatsBySlot(bookings)

        val result = mutableListOf<DayAvailability>()
        var date = startDate
        while (date.isBefore(endDateExclusive)) {
            val availableSlots = mutableListOf<TimeSlot>()
            for (slot in slots) {
                val occupiedSeats: Set<String> = occupiedSeatsBySlot[date to slot.id] ?: emptySet()
                for (seat in seats) {
                    if (seat.id !in occupiedSeats) {
                        availableSlots.add(slot)
                        break
                    }
                }
            }
            result.add(DayAvailability(date, availableSlots))
            date = date.plusDays(1)
        }
        return result
    }

    override suspend fun getSeatSlots(date: LocalDate, timeSlot: TimeSlot): List<SeatSlot> {
        return database.withTransaction {
            val storedSlot = requireNotNull(database.timeSlotDao().getById(timeSlot.id)) {
                "Unknown time slot: ${timeSlot.id}"
            }.toModel()
            val occupiedSeats = database.bookingDao().getForTimeSlot(date, storedSlot.id)
                .map { it.seatId }.toSet()
            database.seatDao().getAll().map { seat ->
                SeatSlot(
                    seat = seat.toModel(),
                    date = date,
                    timeSlot = storedSlot,
                    status = if (seat.id in occupiedSeats) {
                        AvailabilityStatus.OCCUPIED
                    } else {
                        AvailabilityStatus.AVAILABLE
                    },
                )
            }
        }
    }

    private suspend fun getSuitableSeats(requiresComputer: Boolean): List<SeatEntity> {
        val seats = mutableListOf<SeatEntity>()
        for (seat in database.seatDao().getAll()) {
            if (requiresComputer && seat.type != SeatType.COMPUTER) {
                continue
            }
            seats.add(seat)
        }
        return seats
    }

    private fun getOccupiedSeatsBySlot(
        bookings: List<BookingEntity>,
    ): Map<Pair<LocalDate, String>, Set<String>> {
        // Each date and interval pair has its own set of occupied seat IDs.
        val occupiedSeatsBySlot = mutableMapOf<Pair<LocalDate, String>, MutableSet<String>>()
        for (booking in bookings) {
            val key = booking.date to booking.timeSlotId
            var occupiedSeats = occupiedSeatsBySlot[key]
            if (occupiedSeats == null) {
                occupiedSeats = mutableSetOf()
                occupiedSeatsBySlot[key] = occupiedSeats
            }
            occupiedSeats.add(booking.seatId)
        }
        return occupiedSeatsBySlot
    }

    private fun SeatEntity.toModel() = Seat(id, label, type, position)

    private fun TimeSlotEntity.toModel() = TimeSlot(id, startTime, endTime)
}
