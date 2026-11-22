package com.example.studyspot.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.DayAvailability
// import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.domain.repository.SeatRepository
import com.example.studyspot.presentation.state.SeatMapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SeatMapViewModel @Inject constructor(
    private val repository: SeatRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(SeatMapUiState())
    val state: StateFlow<SeatMapUiState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var availabilityJob: Job? = null

    init {
        refreshAvailability()
    }

    fun refreshAvailability() {
        availabilityJob?.cancel()
        loadJob?.cancel()
        val requiresComputer = _state.value.requiresComputer
        _state.value = _state.value.copy(
            isAvailabilityLoading = true,
            availabilityErrorMessage = null,
            seatSlots = emptyList(),
            selectedSeatId = null,
            isLoading = false,
            errorMessage = null,
        )
        availabilityJob = viewModelScope.launch {
            try {
                val today = LocalDate.now(clock)
                val timeSlots = repository.getTimeSlots()
                val days = repository.getAvailability(today, today.plusMonths(1), requiresComputer)
                currentCoroutineContext().ensureActive()
                val now = LocalTime.now(clock)
                val availableDays = mutableListOf<DayAvailability>()
                for (day in days) {
                    val intervals = mutableListOf<TimeSlot>()
                    for (slot in day.availableTimeSlots) {
                        if (day.date != today || slot.startTime.isAfter(now)) {
                            intervals.add(slot)
                        }
                    }
                    availableDays.add(day.copy(availableTimeSlots = intervals))
                }




                val previous = _state.value
                val selectedDay = availableDays.firstOrNull {
                    it.date == previous.selectedDate && it.availableTimeSlots.isNotEmpty()
                }
                val selectedSlot = selectedDay?.availableTimeSlots?.firstOrNull {
                    it.id == previous.selectedTimeSlot?.id
                }
                _state.value = previous.copy(
                    timeSlots = timeSlots,
                    availableDays = availableDays,
                    isAvailabilityLoading = false,
                    selectedDate = selectedDay?.date,
                    selectedTimeSlot = selectedSlot,
                )
                if (selectedDay != null && selectedSlot != null) {
                    loadSeatSlots(selectedDay.date, selectedSlot)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isAvailabilityLoading = false,
                    availableDays = emptyList(),
                    selectedDate = null,
                    selectedTimeSlot = null,
                    availabilityErrorMessage = "Не удалось загрузить доступные даты. Попробуйте ещё раз.",
                )
            }
        }
    }

    fun onComputerRequiredChange(requiresComputer: Boolean) {
        if (_state.value.requiresComputer == requiresComputer) return
        _state.value = _state.value.copy(requiresComputer = requiresComputer)
        refreshAvailability()
    }

    fun onDateSelected(date: LocalDate) {
        val current = _state.value
        if (current.selectedDate == date) return
        loadJob?.cancel()
        _state.value = current.copy(
            selectedDate = date,
            selectedTimeSlot = null,
            selectedSeatId = null,
            seatSlots = emptyList(),
            isLoading = false,
            errorMessage = null,
        )
    }

    fun onTimeSlotSelected(timeSlotId: String) {
        val current = _state.value
        val date = current.selectedDate ?: return
        val day = current.availableDays.firstOrNull { it.date == date } ?: return
        val slot = day.availableTimeSlots.firstOrNull { it.id == timeSlotId } ?: return
        if (date == LocalDate.now(clock) && !slot.startTime.isAfter(LocalTime.now(clock))) {
            refreshAvailability()
            return
        }
        loadSeatSlots(date, slot)
    }

    fun resetSelection() {
        _state.value = SeatMapUiState()
        refreshAvailability()
    }

    fun loadSeatSlots(date: LocalDate, timeSlot: TimeSlot) {
        // A new selection replaces the previous request.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                selectedDate = date,
                selectedTimeSlot = timeSlot,
                selectedSeatId = null,
                seatSlots = emptyList(),
                isLoading = true,
                errorMessage = null,
            )

            try {
                val seatSlots = repository.getSeatSlots(date, timeSlot)
                currentCoroutineContext().ensureActive()
                _state.value = _state.value.copy(seatSlots = seatSlots, isLoading = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Не удалось загрузить места. Попробуйте ещё раз.",
                )
            }
        }
    }

    fun onSeatClick(seatId: String) {
        val currentState = _state.value

        val selectedSeatId = if (currentState.selectedSeatId == seatId) {
            null
        } else {
            seatId
        }

        _state.value = currentState.copy(selectedSeatId = selectedSeatId)
    }
}
