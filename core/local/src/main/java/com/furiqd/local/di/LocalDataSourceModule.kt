package com.furiqd.local.di

import com.furiqd.local.contract.ChallengeLocalDataSource
import com.furiqd.local.contract.ImageLocalDataSource
import com.furiqd.local.contract.PairingLocalDataSource
import com.furiqd.local.contract.TokenLocalDataSource
import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.local.datasource.ImageLocalDataSourceImpl
import com.furiqd.local.datasource.PairingLocalDataSourceImpl
import com.furiqd.local.datasource.TokenLocalDataSourceImpl
import com.furiqd.local.datasource.UserLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindUserLocalDataSource(
        userLocalDataSourceImpl: UserLocalDataSourceImpl
    ): UserLocalDataSource

    @Binds
    @Singleton
    abstract fun bindTokenLocalDataSource(
        tokenLocalDataSourceImpl: TokenLocalDataSourceImpl
    ): TokenLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPairingLocalDataSource(
        avatarLocalDataSourceImpl: PairingLocalDataSourceImpl
    ): PairingLocalDataSource

    @Binds
    @Singleton
    abstract fun bindImageLocalDataSource(
        imageLocalDataSourceImpl: ImageLocalDataSourceImpl
    ): ImageLocalDataSource
}