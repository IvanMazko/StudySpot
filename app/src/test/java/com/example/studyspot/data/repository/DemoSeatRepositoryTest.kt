package com.example.studyspot.data.repository

import com.example.studyspot.data.demo.DemoHallData
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.SeatType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSeatRepositoryTest {
    private val repository = DemoSeatRepository()
    private val date = DemoHallData.demoStartDate
    private val morning = DemoHallData.timeSlots[0]
    private val afternoon = DemoHallData.timeSlots[1]
    private val evening = DemoHallData.timeSlots[2]

    @Test
    fun calendarAvailabilityRespectsFilterAndOccupiedIntervals() = runTest {
        val allSeats = repository.getAvailability(date, date.plusDays(5), false)
        val computers = repository.getAvailability(date, date.plusDays(5), true)
        assertEquals(5, allSeats.size)
        assertEquals(date.plusDays(4), allSeats.last().date)
        assertTrue(allSeats[1].availableTimeSlots.isNotEmpty())
        assertTrue(computers[1].availableTimeSlots.isEmpty())
        assertEquals(listOf("morning", "evening"), allSeats[2].availableTimeSlots.map { it.id })
        assertTrue(allSeats[3].availableTimeSlots.isEmpty())
        assertEquals(3, allSeats[4].availableTimeSlots.size)
    }

    @Test
    fun sameSeatCanBeOccupiedInMorningAndAvailableInEvening() = runTest {
        val morningSlots = repository.getSeatSlots(date, morning)
        val eveningSlots = repository.getSeatSlots(date, evening)

        assertEquals(AvailabilityStatus.AVAILABLE, morningSlots.first { it.seat.id == "seat-1" }.status)
        assertEquals(AvailabilityStatus.OCCUPIED, morningSlots.first { it.seat.id == "seat-2" }.status)
        assertEquals(AvailabilityStatus.AVAILABLE, eveningSlots.first { it.seat.id == "seat-2" }.status)
        assertEquals(DemoHallData.seats, morningSlots.map { it.seat })
        for (slot in morningSlots) {
            assertEquals(date, slot.date)
            assertEquals(morning, slot.timeSlot)
        }
    }

    @Test
    fun nextDayOnlyComputerSeatsAreOccupied() = runTest {
        for (timeSlot in DemoHallData.timeSlots) {
            val slots = repository.getSeatSlots(date.plusDays(1), timeSlot)
            assertTrue(slots.any { it.seat.type == SeatType.COMPUTER })
            assertTrue(slots.any { it.seat.type == SeatType.REGULAR })
            for (slot in slots) {
                val expected = if (slot.seat.type == SeatType.COMPUTER) {
                    AvailabilityStatus.OCCUPIED
                } else {
                    AvailabilityStatus.AVAILABLE
                }
                assertEquals(expected, slot.status)
            }
        }
    }

    @Test
    fun fullyOccupiedAfternoonDoesNotAffectOtherIntervals() = runTest {
        val occupied = repository.getSeatSlots(date.plusDays(2), afternoon)
        val available = repository.getSeatSlots(date.plusDays(2), morning)

        assertEquals(DemoHallData.seats.size, occupied.size)
        assertTrue(occupied.all { it.status == AvailabilityStatus.OCCUPIED })
        assertTrue(available.all { it.status == AvailabilityStatus.AVAILABLE })
    }

    @Test
    fun fullyOccupiedDayHasNoAvailableSeats() = runTest {
        for (timeSlot in DemoHallData.timeSlots) {
            val slots = repository.getSeatSlots(date.plusDays(3), timeSlot)
            assertEquals(DemoHallData.seats.size, slots.size)
            assertTrue(slots.all { it.status == AvailabilityStatus.OCCUPIED })
        }
    }

    @Test
    fun dateWithoutBookingsHasAllSeatsAvailable() = runTest {
        for (timeSlot in DemoHallData.timeSlots) {
            val slots = repository.getSeatSlots(date.plusDays(4), timeSlot)
            assertEquals(DemoHallData.seats.size, slots.size)
            assertTrue(slots.all { it.status == AvailabilityStatus.AVAILABLE })
        }
    }
}
