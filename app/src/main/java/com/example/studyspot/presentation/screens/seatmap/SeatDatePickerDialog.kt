package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.studyspot.R
import com.example.studyspot.domain.models.DayAvailability
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatDatePickerDialog(
    selectedDate: LocalDate?,
    availableDays: List<DayAvailability>,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    if (availableDays.isEmpty()) return
    val startDate = availableDays.first().date
    val lastDate = availableDays.last().date
    val availableDates = remember(availableDays) {
        availableDays.filter { it.availableTimeSlots.isNotEmpty() }.map { it.date }.toSet()
    }
    // DatePicker represents calendar days as UTC midnight, irrespective of the device's zone.
    val selectableDates = remember(availableDates, startDate, lastDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return date in availableDates
            }

            override fun isSelectableYear(year: Int): Boolean = year in startDate.year..lastDate.year
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        initialDisplayedMonthMillis = (selectedDate ?: startDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = selectableDates,
        yearRange = startDate.year..lastDate.year,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() in availableDates
                } == true,
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (date !in availableDates) return@TextButton
                        onDateSelected(date)
                        onDismiss()
                    }
                },
            ) { Text(stringResource(R.string.confirm_date)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
