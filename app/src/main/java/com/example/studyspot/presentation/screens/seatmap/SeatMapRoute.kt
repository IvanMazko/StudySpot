package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspot.presentation.viewmodels.SeatMapViewModel

@Composable
fun SeatMapRoute(
    modifier: Modifier = Modifier,
    viewModel: SeatMapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SeatMapScreen(
        state = state,
        onSeatClick = viewModel::onSeatClick,
        onComputerRequiredChange = viewModel::onComputerRequiredChange,
        onDateSelected = viewModel::onDateSelected,
        onTimeSlotSelected = viewModel::onTimeSlotSelected,
        onResetClick = viewModel::resetSelection,
        onAvailabilityRetryClick = viewModel::refreshAvailability,
        onRetryClick = {
            state.selectedTimeSlot?.let { viewModel.onTimeSlotSelected(it.id) }
        },
        modifier = modifier,
    )
}
