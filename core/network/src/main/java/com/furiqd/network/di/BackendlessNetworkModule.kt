package com.furiqd.network.di

import com.furiqd.network.BuildConfig
import com.furiqd.network.datasource.BackendlessApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackendlessNetworkModule {
    private const val BACKENDLESS_APP_ID = BuildConfig.BACKENDLESS_APP_ID
    private const val BACKENDLESS_REST_KEY = BuildConfig.BACKENDLESS_REST_KEY
    private const val BASE_URL = "https://api.backendless.com/$BACKENDLESS_APP_ID/$BACKENDLESS_REST_KEY/"
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideBackendlessApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): BackendlessApi {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(BackendlessApi::class.java)
    }
}