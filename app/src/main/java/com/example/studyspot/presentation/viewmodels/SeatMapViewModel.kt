package com.example.studyspot.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SeatMapViewModel @Inject constructor(
    private val repository: SeatRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SeatMapUiState())
    val state: StateFlow<SeatMapUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun loadSeatSlots(date: LocalDate, timeSlot: TimeSlot) {
        // A new selection replaces the previous request.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = SeatMapUiState(isLoading = true)

            try {
                val seatSlots = repository.getSeatSlots(date, timeSlot)
                _state.value = SeatMapUiState(seatSlots = seatSlots)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = SeatMapUiState(
                    errorMessage = "Не удалось загрузить места. Попробуйте ещё раз.",
                )
            }
        }
    }
}
