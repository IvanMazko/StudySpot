package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.studyspot.R
import com.example.studyspot.domain.models.TimeSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeatSearchPanel(
    requiresComputer: Boolean,
    selectedDate: LocalDate?,
    timeSlots: List<TimeSlot>,
    selectedTimeSlotId: String?,
    availableTimeSlotIds: Set<String>,
    onComputerRequiredChange: (Boolean) -> Unit,
    onDateClick: () -> Unit,
    onTimeSlotClick: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .toggleable(requiresComputer, role = Role.Checkbox, onValueChange = onComputerRequiredChange)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = requiresComputer, onCheckedChange = null)
            Text(text = stringResource(R.string.only_computer_seats))
        }
        OutlinedButton(onClick = onDateClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            val label = if (selectedDate == null) {
                stringResource(R.string.choose_date)
            } else {
                stringResource(R.string.selected_date, selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
            }
            Text(text = label)
        }
        Text(text = stringResource(R.string.choose_time_slot), style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val formatter = DateTimeFormatter.ofPattern("H:mm")
            for (slot in timeSlots) {
                val selected = slot.id == selectedTimeSlotId
                Button(
                    onClick = { onTimeSlotClick(slot.id) },
                    enabled = enabled && selectedDate != null && slot.id in availableTimeSlotIds,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.time_slot_range, slot.startTime.format(formatter), slot.endTime.format(formatter)),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
