package com.example.studyspot.di

import android.content.Context
import androidx.room.Room
import com.example.studyspot.data.local.StudySpotDatabase
import com.example.studyspot.data.local.dao.BookingDao
import com.example.studyspot.data.local.dao.SeatDao
import com.example.studyspot.data.local.dao.TimeSlotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudySpotDatabase =
        Room.databaseBuilder(
            context,
            StudySpotDatabase::class.java,
            "study_spot.db",
        ).build()

    @Provides
    @Singleton
    fun provideSeatDao(database: StudySpotDatabase): SeatDao = database.seatDao()

    @Provides
    @Singleton
    fun provideTimeSlotDao(database: StudySpotDatabase): TimeSlotDao = database.timeSlotDao()

    @Provides
    @Singleton
    fun provideBookingDao(database: StudySpotDatabase): BookingDao = database.bookingDao()
}
