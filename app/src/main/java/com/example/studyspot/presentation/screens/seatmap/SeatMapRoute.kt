package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.presentation.viewmodels.SeatMapViewModel
import java.time.LocalDate

@Composable
fun SeatMapRoute(
    date: LocalDate,
    timeSlot: TimeSlot,
    modifier: Modifier = Modifier,
    viewModel: SeatMapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(date, timeSlot) {
        viewModel.loadSeatSlots(date, timeSlot)
    }

    SeatMapScreen(
        state = state,
        onSeatClick = viewModel::onSeatClick,
        onRetryClick = { viewModel.loadSeatSlots(date, timeSlot) },
        modifier = modifier,
    )
}
