package com.furiqd.network.di

import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.contract.RemoteImageDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.datasource.BackendlessChallengeApi
import com.furiqd.network.datasource.ChallengeRemoteDataSourceImpl
import com.furiqd.network.datasource.FirebaseAuthDataSource
import com.furiqd.network.datasource.FirebaseRoomDataSource
import com.furiqd.network.datasource.GoogleStorageDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    // Единый OkHttpClient для приложения с логгером и таймаутами
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteAuthDataSource(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): RemoteAuthDataSource {
        return FirebaseAuthDataSource(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideRemotePairingDataSource(
        firestore: FirebaseFirestore
    ): RemoteRoomDataSource {
        return FirebaseRoomDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRemoteImageDataSource(
        @Named("workerEndpoint") workerEndpoint: String,
        okHttpClient: OkHttpClient
    ): RemoteImageDataSource {
        return GoogleStorageDataSource(workerEndpoint, okHttpClient)
    }

    @Provides
    @Singleton
    fun provideRemoteChallengeDataSource(
        backendlessApi: BackendlessChallengeApi
    ): RemoteChallengeDataSource {
        return ChallengeRemoteDataSourceImpl(backendlessApi)
    }
}