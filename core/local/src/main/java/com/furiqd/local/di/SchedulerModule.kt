package com.furiqd.local.di

import com.furiqd.local.scheduler.StreakExpirationScheduler
import com.furiqd.local.scheduler.StreakExpirationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    abstract fun bindStreakExpirationScheduler(
        streakExpirationSchedulerImpl: StreakExpirationSchedulerImpl
    ): StreakExpirationScheduler
}