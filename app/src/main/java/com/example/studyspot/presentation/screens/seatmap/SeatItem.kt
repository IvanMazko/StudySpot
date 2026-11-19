package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.studyspot.R
import com.example.studyspot.domain.models.AvailabilityStatus
import com.example.studyspot.domain.models.SeatSlot
import com.example.studyspot.domain.models.SeatType

@Composable
fun SeatItem(
    seatSlot: SeatSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAvailable = seatSlot.status == AvailabilityStatus.AVAILABLE
    val status = stringResource(if (isAvailable) R.string.seat_available else R.string.seat_occupied)
    val type = stringResource(
        if (seatSlot.seat.type == SeatType.COMPUTER) R.string.seat_computer else R.string.seat_regular,
    )
    val description = stringResource(R.string.seat_description, seatSlot.seat.label, type, status)

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) { contentDescription = description }
            .selectable(
                selected = isSelected,
                enabled = isAvailable,
                role = Role.Checkbox,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SeatSymbol(isAvailable, isSelected, seatSlot.seat.type == SeatType.COMPUTER)
        Text(text = seatSlot.seat.label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun SeatSymbol(
    isAvailable: Boolean = true,
    isSelected: Boolean = false,
    hasComputer: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    val borderColor = colorResource(
        if (isSelected) R.color.seat_selected_border else R.color.seat_border,
    )
    val backgroundColor = colorResource(
        if (isAvailable) R.color.seat_available_background else R.color.seat_occupied_background,
    )
    Box(
        modifier = modifier
            .size(52.dp)
            .background(backgroundColor, shape)
            .border(BorderStroke(if (isSelected) 3.dp else 1.dp, borderColor), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (hasComputer) {
            Icon(
                painter = painterResource(R.drawable.ic_computer),
                contentDescription = null,
                tint = colorResource(R.color.seat_computer_icon),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
