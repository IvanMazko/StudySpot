package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.studyspot.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeatMapLegend(modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LegendEntry(stringResource(R.string.seat_available)) { SeatSymbol(modifier = it) }
        LegendEntry(stringResource(R.string.seat_occupied)) { SeatSymbol(isAvailable = false, modifier = it) }
        LegendEntry(stringResource(R.string.seat_selected)) { SeatSymbol(isSelected = true, modifier = it) }
        LegendEntry(stringResource(R.string.seat_computer)) { SeatSymbol(hasComputer = true, modifier = it) }
    }
}

@Composable
private fun LegendEntry(label: String, symbol: @Composable (Modifier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        symbol(Modifier.size(24.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
