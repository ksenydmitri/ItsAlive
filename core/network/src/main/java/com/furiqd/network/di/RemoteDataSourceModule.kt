package com.furiqd.network.di

import android.content.Context
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.contract.RemoteImageDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.datasource.BackendlessApi
import com.furiqd.network.datasource.ChallengeRemoteDataSourceImpl
import com.furiqd.network.datasource.FirebaseAuthDataSource
import com.furiqd.network.datasource.FirebaseRoomDataSource
import com.furiqd.network.datasource.GoogleStorageDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
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
        firestore: FirebaseFirestore,
        backendlessApi: BackendlessApi,
        @ApplicationContext context: Context
    ): RemoteAuthDataSource {
        return FirebaseAuthDataSource(auth, firestore, backendlessApi,context)
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
        backendlessApi: BackendlessApi
    ): RemoteChallengeDataSource {
        return ChallengeRemoteDataSourceImpl(backendlessApi)
    }
}