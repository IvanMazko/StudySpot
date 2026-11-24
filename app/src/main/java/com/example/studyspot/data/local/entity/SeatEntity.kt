package com.example.studyspot.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studyspot.domain.models.SeatPosition
import com.example.studyspot.domain.models.SeatType

@Entity(tableName = "seats")
data class SeatEntity(
    @PrimaryKey val id: String,
    val label: String,
    val type: SeatType,
    @Embedded val position: SeatPosition,
)
