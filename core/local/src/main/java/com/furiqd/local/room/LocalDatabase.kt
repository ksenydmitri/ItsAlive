package com.furiqd.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.furiqd.local.entity.DailyChallengeEntity
import com.furiqd.local.entity.JointPetEntity
import com.furiqd.local.entity.LocalUserEntity

@Database(entities = [JointPetEntity::class, LocalUserEntity::class, DailyChallengeEntity::class],
    version = 7, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun userDao(): UserDao
    abstract fun challengeDao(): DailyChallengeDao

}
