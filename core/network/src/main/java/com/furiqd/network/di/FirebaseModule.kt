package com.furiqd.network.di

import com.furiqd.network.BuildConfig
import com.furiqd.network.datasource.AppFirebaseMessagingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Named("workerEndpoint")
    fun provideWorkerEndpoint(): String {
        return BuildConfig.WORKER_ENDPOINT
    }

    @Provides
    @Singleton
    fun provideFirebaseMessagingService(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}