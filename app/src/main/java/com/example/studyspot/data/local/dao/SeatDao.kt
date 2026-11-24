package com.example.studyspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.studyspot.data.local.entity.SeatEntity

@Dao
interface SeatDao {

    @Query("SELECT * FROM seats ORDER BY `row`, `column`")
    suspend fun getAll(): List<SeatEntity>

    @Query("SELECT * FROM seats WHERE id = :id")
    suspend fun getById(id: String): SeatEntity?

    @Insert
    suspend fun insertAll(seats: List<SeatEntity>)
}
