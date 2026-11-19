package com.example.studyspot.presentation.screens.seatmap

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberScrollable2DState
import androidx.compose.foundation.gestures.scrollable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.studyspot.domain.models.SeatSlot

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeatMap(
    seatSlots: List<SeatSlot>,
    selectedSeatId: String?,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Empty coordinates remain empty: places never reflow to fit the screen.
    var rowCount = 0
    var columnCount = 0
    for (slot in seatSlots) {
        rowCount = maxOf(rowCount, slot.seat.position.row + 1)
        columnCount = maxOf(columnCount, slot.seat.position.column + 1)
    }
    val cellWidth = 84.dp
    val cellHeight = 100.dp
    val mapWidth = cellWidth * columnCount + 32.dp
    val mapHeight = cellHeight * rowCount + 32.dp
    val density = LocalDensity.current

    BoxWithConstraints(modifier.clipToBounds().background(MaterialTheme.colorScheme.surfaceVariant)) {
        val maxX = with(density) { (mapWidth - maxWidth).coerceAtLeast(0.dp).toPx() }
        val maxY = with(density) { (mapHeight - maxHeight).coerceAtLeast(0.dp).toPx() }
        var pan by remember { mutableStateOf(Offset.Zero) }
        val boundedPan = Offset(pan.x.coerceIn(-maxX, 0f), pan.y.coerceIn(-maxY, 0f))
        val scrollState = rememberScrollable2DState { delta ->
            val previous = Offset(pan.x.coerceIn(-maxX, 0f), pan.y.coerceIn(-maxY, 0f))
            pan = Offset(
                (previous.x + delta.x).coerceIn(-maxX, 0f),
                (previous.y + delta.y).coerceIn(-maxY, 0f),
            )
            pan - previous
        }

        Box(Modifier.fillMaxSize().scrollable2D(scrollState)) {
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .size(mapWidth, mapHeight)
                    .graphicsLayer {
                        translationX = boundedPan.x
                        translationY = boundedPan.y
                    },
            ) {
                for (slot in seatSlots) {
                    key(slot.seat.id) {
                        SeatItem(
                            seatSlot = slot,
                            isSelected = slot.seat.id == selectedSeatId,
                            onClick = { onSeatClick(slot.seat.id) },
                            modifier = Modifier.offset(
                                x = 16.dp + cellWidth * slot.seat.position.column,
                                y = 16.dp + cellHeight * slot.seat.position.row,
                            ),
                        )
                    }
                }
            }
        }
    }
}
