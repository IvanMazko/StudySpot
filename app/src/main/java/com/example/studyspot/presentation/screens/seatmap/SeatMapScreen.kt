package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studyspot.R
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.presentation.state.SeatMapUiState
import com.example.studyspot.presentation.theme.StudySpotTheme

@Composable
fun SeatMapScreen(
    state: SeatMapUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()

            state.errorMessage != null -> {
                Text(text = state.errorMessage)
                Button(onClick = onRetryClick) {
                    Text(text = stringResource(R.string.retry))
                }
            }

            state.seatSlots.isEmpty() -> Text(text = stringResource(R.string.no_seats))

            else -> {
                for (seatSlot in state.seatSlots) {
                    val statusResource = if (seatSlot.status == AvailabilityStatus.AVAILABLE) {
                        R.string.seat_available
                    } else {
                        R.string.seat_occupied
                    }
                    Text(
                        text = stringResource(
                            R.string.seat_status,
                            seatSlot.seat.label,
                            stringResource(statusResource),
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeatMapScreenPreview() {
    StudySpotTheme {
        SeatMapScreen(state = SeatMapUiState(), onRetryClick = {})
    }
}
