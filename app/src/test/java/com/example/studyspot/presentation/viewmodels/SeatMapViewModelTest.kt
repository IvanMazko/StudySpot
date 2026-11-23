package com.example.studyspot.presentation.viewmodels

import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.DayAvailability
import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import java.time.LocalDate
import java.time.Clock
import java.time.ZoneOffset
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeatMapViewModelTest {
    private val date = LocalDate.of(2026, 11, 18)
    private lateinit var viewModel: SeatMapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val clock = Clock.fixed(date.atTime(10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        viewModel = SeatMapViewModel(TestSeatRepository(), clock)
        viewModel.onDateSelected(date.plusDays(1))
        viewModel.onTimeSlotSelected("morning")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clickingAvailableSeatSelectsIt() {
        viewModel.onSeatClick("seat-1")

        assertEquals("seat-1", viewModel.state.value.selectedSeatId)
        assertEquals(AvailabilityStatus.AVAILABLE, viewModel.state.value.seatSlots[0].status)
    }

    @Test
    fun clickingSelectedSeatClearsSelection() {
        viewModel.onSeatClick("seat-1")
        viewModel.onSeatClick("seat-1")

        assertNull(viewModel.state.value.selectedSeatId)
    }

    @Test
    fun clickingAnotherAvailableSeatReplacesSelection() {
        viewModel.onSeatClick("seat-1")
        viewModel.onSeatClick("seat-3")

        assertEquals("seat-3", viewModel.state.value.selectedSeatId)
    }

    @Test
    fun selectingAnotherIntervalClearsSeatSelectionAndReloadsMap() {
        viewModel.onSeatClick("seat-1")

        viewModel.onTimeSlotSelected("evening")

        assertNull(viewModel.state.value.selectedSeatId)
        assertEquals("evening", viewModel.state.value.selectedTimeSlot?.id)
        assertEquals(3, viewModel.state.value.seatSlots.size)
        assertTrue(viewModel.state.value.seatSlots.all {
            it.date == date.plusDays(1) && it.timeSlot.id == "evening"
        })
    }

    @Test
    fun calendarExcludesStartedIntervalsAndHasOneMonthWindow() {
        val days = viewModel.state.value.availableDays
        assertEquals(date, days.first().date)
        assertEquals(date.plusMonths(1).minusDays(1), days.last().date)
        assertEquals(listOf("evening"), days.first().availableTimeSlots.map { it.id })
    }

    @Test
    fun selectingAnotherDateClearsTimeSeatAndMap() {
        viewModel.onSeatClick("seat-1")
        viewModel.onDateSelected(date.plusDays(2))
        assertEquals(date.plusDays(2), viewModel.state.value.selectedDate)
        assertNull(viewModel.state.value.selectedTimeSlot)
        assertNull(viewModel.state.value.selectedSeatId)
        assertTrue(viewModel.state.value.seatSlots.isEmpty())
    }

    @Test
    fun selectingSameDatePreservesTimeSeatAndMap() {
        viewModel.onSeatClick("seat-1")
        val before = viewModel.state.value

        viewModel.onDateSelected(date.plusDays(1))

        assertEquals(before, viewModel.state.value)
    }

    @Test
    fun selectingAvailableIntervalLoadsMapAndUnknownIntervalIsIgnored() {
        viewModel.onDateSelected(date.plusDays(1))
        viewModel.onTimeSlotSelected("morning")
        assertEquals("morning", viewModel.state.value.selectedTimeSlot?.id)
        assertEquals(3, viewModel.state.value.seatSlots.size)
        val before = viewModel.state.value
        viewModel.onTimeSlotSelected("unknown")
        assertEquals(before, viewModel.state.value)
    }

    @Test
    fun computerFilterClearsDateTimeSeatAndMapWhenDayBecomesUnavailable() {
        viewModel.onDateSelected(date.plusDays(1))
        viewModel.onTimeSlotSelected("morning")
        viewModel.onSeatClick("seat-1")
        viewModel.onComputerRequiredChange(true)
        // This day has no computer availability at all.
        assertTrue(viewModel.state.value.requiresComputer)
        assertNull(viewModel.state.value.selectedDate)
        assertNull(viewModel.state.value.selectedTimeSlot)
        assertNull(viewModel.state.value.selectedSeatId)
        assertTrue(viewModel.state.value.seatSlots.isEmpty())
    }

    @Test
    fun computerFilterPreservesValidDateAndTimeAndReloadsMap() {
        viewModel.onDateSelected(date.plusDays(2))
        viewModel.onTimeSlotSelected("evening")
        viewModel.onSeatClick("seat-1")
        viewModel.onComputerRequiredChange(true)
        assertEquals(date.plusDays(2), viewModel.state.value.selectedDate)
        assertEquals("evening", viewModel.state.value.selectedTimeSlot?.id)
        assertNull(viewModel.state.value.selectedSeatId)
        assertEquals(3, viewModel.state.value.seatSlots.size)
        viewModel.onSeatClick("seat-3")
        assertEquals("seat-3", viewModel.state.value.selectedSeatId)
    }

    @Test
    fun computerFilterClearsInvalidTimeWhileKeepingAvailableDate() {
        viewModel.onDateSelected(date.plusDays(2))
        viewModel.onTimeSlotSelected("morning")
        viewModel.onComputerRequiredChange(true)
        assertEquals(date.plusDays(2), viewModel.state.value.selectedDate)
        assertNull(viewModel.state.value.selectedTimeSlot)
        assertTrue(viewModel.state.value.seatSlots.isEmpty())
    }

    @Test
    fun refreshPreservesFilterDateAndTimeButClearsSeatSelection() {
        viewModel.onDateSelected(date.plusDays(2))
        viewModel.onTimeSlotSelected("evening")
        viewModel.onComputerRequiredChange(true)
        viewModel.onSeatClick("seat-3")

        viewModel.refreshAvailability()

        assertTrue(viewModel.state.value.requiresComputer)
        assertEquals(date.plusDays(2), viewModel.state.value.selectedDate)
        assertEquals("evening", viewModel.state.value.selectedTimeSlot?.id)
        assertNull(viewModel.state.value.selectedSeatId)
        assertEquals(3, viewModel.state.value.seatSlots.size)
        assertFalse(viewModel.state.value.isAvailabilityLoading)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun resetClearsParametersAndReloadsUnfilteredAvailability() {
        viewModel.onDateSelected(date.plusDays(2))
        viewModel.onTimeSlotSelected("evening")
        viewModel.onComputerRequiredChange(true)
        viewModel.onSeatClick("seat-3")

        viewModel.resetSelection()

        assertFalse(viewModel.state.value.requiresComputer)
        assertNull(viewModel.state.value.selectedDate)
        assertNull(viewModel.state.value.selectedTimeSlot)
        assertNull(viewModel.state.value.selectedSeatId)
        assertTrue(viewModel.state.value.seatSlots.isEmpty())
        assertTrue(viewModel.state.value.availableDays.isNotEmpty())
        assertEquals(listOf("morning", "evening"), viewModel.state.value.availableDays[1].availableTimeSlots.map { it.id })
        assertFalse(viewModel.state.value.isAvailabilityLoading)
    }

    private class TestSeatRepository : SeatRepository {
        override suspend fun getTimeSlots(): List<TimeSlot> = listOf(
            TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0)),
            TimeSlot("evening", LocalTime.of(15, 0), LocalTime.of(18, 0)),
        )
        override suspend fun getAvailability(
            startDate: LocalDate,
            endDateExclusive: LocalDate,
            requiresComputer: Boolean,
        ): List<DayAvailability> {
            val morning = TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0))
            val evening = TimeSlot("evening", LocalTime.of(15, 0), LocalTime.of(18, 0))
            val result = mutableListOf<DayAvailability>()
            var day = startDate
            while (day.isBefore(endDateExclusive)) {
                val slots = if (!requiresComputer) {
                    listOf(morning, evening)
                } else if (day == startDate.plusDays(1)) {
                    emptyList()
                } else {
                    listOf(evening)
                }
                result.add(DayAvailability(day, slots))
                day = day.plusDays(1)
            }
            return result
        }

        override suspend fun getSeatSlots(date: LocalDate, timeSlot: TimeSlot): List<SeatSlot> {
            val result = mutableListOf<SeatSlot>()
            for (number in 1..3) {
                val seat = Seat(
                    id = "seat-$number",
                    label = number.toString(),
                    type = if (number == 3) SeatType.COMPUTER else SeatType.REGULAR,
                    position = SeatPosition(row = 0, column = number - 1),
                )
                val status = if (number == 2) {
                    AvailabilityStatus.OCCUPIED
                } else {
                    AvailabilityStatus.AVAILABLE
                }
                result.add(SeatSlot(seat, date, timeSlot, status))
            }
            return result
        }
    }
}
