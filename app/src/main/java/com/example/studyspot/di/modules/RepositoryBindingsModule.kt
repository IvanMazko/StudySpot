package com.example.studyspot.di.modules

import com.example.studyspot.data.repository.OfflineRoomRepository
import com.example.studyspot.di.qualifiers.OfflineRepository
import com.example.studyspot.domain.repository.SeatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryBindingsModule {

    @Binds
    @OfflineRepository
    fun bindOfflineSeatRepository(repository: OfflineRoomRepository): SeatRepository
}
