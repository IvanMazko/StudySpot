package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyspot.R
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.Seat
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.presentation.state.SeatMapUiState
import com.example.studyspot.presentation.theme.StudySpotTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun SeatMapScreen(
    state: SeatMapUiState,
    onSeatClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onComputerRequiredChange: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotSelected: (String) -> Unit,
    onResetClick: () -> Unit,
    onAvailabilityRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val selectedDay = state.availableDays.firstOrNull { it.date == state.selectedDate }
    val availableSlotIds = selectedDay?.availableTimeSlots?.map { it.id }?.toSet() ?: emptySet()
    val availabilityReady = !state.isAvailabilityLoading && state.availabilityErrorMessage == null

    if (showDatePicker && availabilityReady) {
        SeatDatePickerDialog(
            selectedDate = state.selectedDate,
            availableDays = state.availableDays,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = stringResource(R.string.hall_map), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        SeatSearchPanel(
            requiresComputer = state.requiresComputer,
            selectedDate = state.selectedDate,
            timeSlots = state.timeSlots,
            selectedTimeSlotId = state.selectedTimeSlot?.id,
            availableTimeSlotIds = availableSlotIds,
            onComputerRequiredChange = onComputerRequiredChange,
            onDateClick = { showDatePicker = true },
            onTimeSlotClick = onTimeSlotSelected,
            enabled = availabilityReady && state.availableDays.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.requiresComputer || state.selectedDate != null) {
            TextButton(onClick = {
                showDatePicker = false
                onResetClick()
            }) { Text(stringResource(R.string.reset_search)) }
        }
        when {
            state.isAvailabilityLoading -> Unit

            state.availabilityErrorMessage != null -> {
                Text(text = state.availabilityErrorMessage)
                Button(onClick = onAvailabilityRetryClick) { Text(stringResource(R.string.retry)) }
            }

            state.availableDays.none { it.availableTimeSlots.isNotEmpty() } -> {
                Text(text = stringResource(R.string.no_matching_dates))
            }

            state.selectedDate == null || state.selectedTimeSlot == null -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(
                        if (state.selectedDate == null) R.string.select_date_and_time_hint else R.string.select_time_hint,
                    ))
                }
            }

            state.isLoading -> Unit

            state.errorMessage != null -> {
                Text(text = state.errorMessage)
                Button(onClick = onRetryClick) {
                    Text(text = stringResource(R.string.retry))
                }
            }

            state.seatSlots.isEmpty() -> Text(text = stringResource(R.string.no_seats))

            else -> {
                SeatMapLegend(Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.map_hint),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
                SeatMap(
                    seatSlots = state.seatSlots,
                    selectedSeatId = state.selectedSeatId,
                    requiresComputer = state.requiresComputer,
                    onSeatClick = onSeatClick,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 360, heightDp = 640, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SeatMapScreenPreview() {
    StudySpotTheme {
        var state by remember {
            val slots = previewSeatSlots()
            val timeSlot = slots.first().timeSlot
            val date = slots.first().date
            mutableStateOf(SeatMapUiState(
                seatSlots = slots,
                selectedSeatId = "seat-1",
                selectedDate = date,
                selectedTimeSlot = timeSlot,
                timeSlots = listOf(timeSlot),
                availableDays = listOf(com.example.studyspot.domain.models.DayAvailability(date, listOf(timeSlot))),
            ))
        }
        SeatMapScreen(
            state = state,
            onSeatClick = { id ->
                state = state.copy(selectedSeatId = if (state.selectedSeatId == id) null else id)
            },
            onRetryClick = {},
            onComputerRequiredChange = { state = state.copy(requiresComputer = it, selectedSeatId = null) },
            onDateSelected = {},
            onTimeSlotSelected = {},
            onResetClick = {},
            onAvailabilityRetryClick = {},
        )
    }
}

// A larger fixture for checking scrolling without changing the repository's demo hall.
private fun previewSeatSlots(): List<SeatSlot> {
    val result = mutableListOf<SeatSlot>()
    val date = LocalDate.of(2026, 11, 19)
    val timeSlot = TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0))
    for (row in 0..9) {
        for (column in 0..7) {
            val number = row * 8 + column + 1
            val seat = Seat(
                id = "seat-$number",
                label = number.toString(),
                type = if (column >= 4) SeatType.COMPUTER else SeatType.REGULAR,
                position = SeatPosition(row, column),
            )
            val status = if (number % 5 == 0) AvailabilityStatus.OCCUPIED else AvailabilityStatus.AVAILABLE
            result.add(SeatSlot(seat, date, timeSlot, status))
        }
    }
    return result
}
