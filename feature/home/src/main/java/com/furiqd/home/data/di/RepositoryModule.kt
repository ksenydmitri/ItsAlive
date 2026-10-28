package com.furiqd.home.data.di

import com.furiqd.home.data.repository.HomeRepositoryImpl
import com.furiqd.home.data.repository.PetDashboardRepositoryImpl
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.home.domain.repository.PetDashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindPetDashboardRepository(
        petDashboardRepositoryImpl: PetDashboardRepositoryImpl
    ): PetDashboardRepository
}