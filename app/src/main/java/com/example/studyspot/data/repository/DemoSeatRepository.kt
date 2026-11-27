package com.example.studyspot.data.repository

import com.example.studyspot.data.demo.DemoHallData
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.DayAvailability
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import java.time.LocalDate

class DemoSeatRepository : SeatRepository {
    override suspend fun getTimeSlots(): List<TimeSlot> = DemoHallData.timeSlots

    override suspend fun getAvailability(
        startDate: LocalDate,
        endDateExclusive: LocalDate,
        requiresComputer: Boolean,
    ): List<DayAvailability> {
        require(endDateExclusive.isAfter(startDate)) {
            "Конец диапазона должен быть позже начала: $startDate — $endDateExclusive"
        }
        val result = mutableListOf<DayAvailability>()
        var date = startDate
        while (date.isBefore(endDateExclusive)) {
            val availableTimeSlots = mutableListOf<TimeSlot>()
            for (timeSlot in DemoHallData.timeSlots) {
                for (seat in DemoHallData.seats) {
                    if (requiresComputer && seat.type != SeatType.COMPUTER) {
                        continue
                    }
                    if (!isOccupied(seat.id, date, timeSlot.id)) {
                        availableTimeSlots.add(timeSlot)
                        break
                    }
                }
            }
            result.add(DayAvailability(date, availableTimeSlots))
            date = date.plusDays(1)
        }
        return result
    }

    override suspend fun getSeatSlots(
        date: LocalDate,
        timeSlot: TimeSlot,
    ): List<SeatSlot> {
        val seatSlots = mutableListOf<SeatSlot>()

        for (seat in DemoHallData.seats) {
            val status = if (isOccupied(seat.id, date, timeSlot.id)) {
                AvailabilityStatus.OCCUPIED
            } else {
                AvailabilityStatus.AVAILABLE
            }

            val seatSlot = SeatSlot(
                seat = seat,
                date = date,
                timeSlot = timeSlot,
                status = status,
            )

            seatSlots.add(seatSlot)
        }
        return seatSlots
    }

    private fun isOccupied(seatId: String, date: LocalDate, timeSlotId: String): Boolean {
        for (occupiedSlot in DemoHallData.occupiedSeatSlots) {
            if (occupiedSlot.seat.id == seatId &&
                occupiedSlot.date == date &&
                occupiedSlot.timeSlot.id == timeSlotId
            ) {
                return true
            }
        }
        return false
    }
}
