package com.example.studyspot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.studyspot.data.local.RoomConverters
import java.time.LocalDate

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = SeatEntity::class,
            parentColumns = ["id"],
            childColumns = ["seatId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = TimeSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeSlotId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["seatId", "date", "timeSlotId"], unique = true),
        Index(value = ["timeSlotId"]),
        Index(value = ["userId"]),
    ],
)
@TypeConverters(RoomConverters::class)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seatId: String,
    val date: LocalDate,
    val timeSlotId: String,
    val userId: String,
)
