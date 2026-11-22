package com.example.studyspot.di

import com.example.studyspot.data.repository.DemoSeatRepository
import com.example.studyspot.domain.repository.SeatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideSeatRepository(): SeatRepository = DemoSeatRepository()

}
