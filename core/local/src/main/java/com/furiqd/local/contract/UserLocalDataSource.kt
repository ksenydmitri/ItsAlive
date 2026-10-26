package com.furiqd.local.contract

import com.furiqd.local.entity.LocalUserEntity
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    val isOnboardingCompletedFlow: Flow<Boolean>
    suspend fun getOnboardingStatus(): Boolean
    suspend fun saveOnboardingStatus(isCompleted: Boolean)
    fun observeUserById(userId: String): Flow<LocalUserEntity?>
    suspend fun getUserById(userId: String): LocalUserEntity?
    suspend fun saveUser(user: LocalUserEntity)
    suspend fun clearLocalData()
    suspend fun getUsersByIds(ids: List<String>): List<LocalUserEntity>
}