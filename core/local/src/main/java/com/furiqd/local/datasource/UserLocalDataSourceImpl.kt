package com.furiqd.local.datasource

import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.local.entity.LocalUserEntity
import com.furiqd.local.room.UserDao
import com.furiqd.local.storage.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserLocalDataSourceImpl @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val userDao: UserDao
) : UserLocalDataSource {

    override val isOnboardingCompletedFlow: Flow<Boolean> =
        preferencesManager.isOnboardingCompleted

    override suspend fun getOnboardingStatus(): Boolean {
        return preferencesManager.isOnboardingCompleted.first()
    }

    override suspend fun saveOnboardingStatus(isCompleted: Boolean) {
        preferencesManager.saveOnboardingStatus(isCompleted)
    }

    override fun observeUser(): Flow<LocalUserEntity?> {
        return userDao.observeUser()
    }

    override suspend fun getUser(): LocalUserEntity? {
        return userDao.getUser()
    }

    override suspend fun saveUser(user: LocalUserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun clearLocalData() {
        userDao.clearUser()
        preferencesManager.clear()
    }

    override suspend fun getUsersByIds(ids: List<String>): List<LocalUserEntity> {
        return userDao.getUsersByIds(ids)
    }
}