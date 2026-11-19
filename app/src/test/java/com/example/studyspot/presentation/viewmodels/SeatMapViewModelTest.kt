package com.example.studyspot.presentation.viewmodels

import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeatMapViewModelTest {
    private val date = LocalDate.of(2026, 11, 18)
    private val timeSlot = TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0))
    private lateinit var viewModel: SeatMapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SeatMapViewModel(TestSeatRepository())
        viewModel.loadSeatSlots(date, timeSlot)
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
    fun clickingOccupiedSeatDoesNotChangeState() {
        val initialState = viewModel.state.value
        viewModel.onSeatClick("seat-2")
        assertEquals(initialState, viewModel.state.value)

        viewModel.onSeatClick("seat-1")
        val selectedState = viewModel.state.value
        viewModel.onSeatClick("seat-2")
        assertEquals(selectedState, viewModel.state.value)
    }

    @Test
    fun clickingUnknownSeatDoesNotChangeState() {
        viewModel.onSeatClick("seat-1")
        val selectedState = viewModel.state.value

        viewModel.onSeatClick("missing")

        assertEquals(selectedState, viewModel.state.value)
    }

    @Test
    fun loadingAnotherDateClearsSelection() {
        viewModel.onSeatClick("seat-1")

        viewModel.loadSeatSlots(date.plusDays(1), timeSlot)

        assertNull(viewModel.state.value.selectedSeatId)
        assertEquals(date.plusDays(1), viewModel.state.value.seatSlots[0].date)
    }

    private class TestSeatRepository : SeatRepository {
        override suspend fun getSeatSlots(date: LocalDate, timeSlot: TimeSlot): List<SeatSlot> {
            val result = mutableListOf<SeatSlot>()
            for (number in 1..3) {
                val seat = Seat(
                    id = "seat-$number",
                    label = number.toString(),
                    type = SeatType.REGULAR,
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
