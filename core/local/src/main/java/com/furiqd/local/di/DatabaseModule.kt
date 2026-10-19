package com.furiqd.local.di

import android.content.Context
import androidx.room.Room
import com.furiqd.local.room.DailyChallengeDao
import com.furiqd.local.room.LocalDatabase
import com.furiqd.local.room.PetDao
import com.furiqd.local.room.UserDao
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
    fun provideLocalDatabase(
        @ApplicationContext context: Context
    ): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "itsalive_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: LocalDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun providePetDao(database: LocalDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: LocalDatabase): DailyChallengeDao {
        return database.challengeDao()
    }
}
