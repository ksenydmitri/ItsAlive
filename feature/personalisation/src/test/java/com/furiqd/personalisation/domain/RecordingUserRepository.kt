
package com.furiqd.personalisation.domain

import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class RecordingUserRepository(
    private val currentUser: User? = null,
    private val updateResults: MutableList<Result<Unit>> = mutableListOf(Result.success(Unit))
) : UserRepository {
    override val sessionState = MutableStateFlow<UserSessionState>(UserSessionState.NotAuthorized)
    var updateCalls = 0
        private set
    var updatedUser: User? = null
        private set
    val updatedUsers = mutableListOf<User>()

    override fun observeUser(): Flow<User?> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun getUser(): User? = currentUser
    override suspend fun syncSession(): Result<Unit> = unsupported()
    override suspend fun saveUser(user: User): Result<Unit> = unsupported()
    override suspend fun logOut(): Result<Unit> = unsupported()
    override suspend fun checkIsOnboardingCompleted(): Boolean = false
    override suspend fun updateUser(user: User): Result<Unit> {
        updateCalls++
        updatedUser = user
        updatedUsers += user
        return if (updateResults.isEmpty()) Result.success(Unit) else updateResults.removeAt(0)
    }
    override suspend fun getUserById(userId: String): User? = null
    override suspend fun updateFcmToken(token: String): Result<Unit> = unsupported()
    override suspend fun isNickNameAvailable(nickName: String): Boolean = true

    private fun unsupported(): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}
