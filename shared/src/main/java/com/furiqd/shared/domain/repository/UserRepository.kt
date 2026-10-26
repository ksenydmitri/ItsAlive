package com.furiqd.shared.domain.repository

import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun observeUser(): Flow<User?>
    suspend fun getUser(): User?
    val sessionState: StateFlow<UserSessionState>
    suspend fun syncSession(): Result<Unit>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun logOut(): Result<Unit>
    suspend fun checkIsOnboardingCompleted(): Boolean
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun getUserById(userId: String): User?
    suspend fun updateFcmToken(token: String): Result<Unit>
}
