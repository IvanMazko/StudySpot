package com.example.studyspot.data.repository

import com.example.studyspot.data.demo.DemoHallData
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import java.time.LocalDate

class DemoSeatRepository : SeatRepository {

    override suspend fun getSeatSlots(
        date: LocalDate,
        timeSlot: TimeSlot,
    ): List<SeatSlot> {
        val seatSlots = mutableListOf<SeatSlot>()

        for (seat in DemoHallData.seats) {
            val status = if (seat.id in DemoHallData.occupiedSeatIds) {
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
}