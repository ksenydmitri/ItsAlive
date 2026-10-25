package com.furiqd.shared.data.repositories

import android.util.Log
import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.shared.data.di.ApplicationScope
import com.furiqd.shared.data.mapper.UserDataMapper
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val TAG = "UserRepositoryImpl"

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAuthDataSource,
    private val localDataSource: UserLocalDataSource,
    private val userMapper: UserDataMapper,
    @ApplicationScope private val scope: CoroutineScope
) : UserRepository {

    override val sessionState: StateFlow<UserSessionState> = remoteDataSource.currentUserIdFlow
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(UserSessionState.NotAuthorized)
            } else {
                syncSessionSilently(userId)
                localDataSource.observeUserById(userId).map { localEntity ->
                    val user = localEntity?.let { userMapper.toDomain(it) }

                    val state = when {
                        user == null -> UserSessionState.Loading
                        user.isOnboardingCompleted -> UserSessionState.FullyAuthorized
                        else -> UserSessionState.NeedsOnboarding
                    }
                    state
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = UserSessionState.Loading
        )
    private suspend fun syncSessionSilently(userId: String) {
        runCatching {
            val remoteUserDto = remoteDataSource.getUserProfile(userId)

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                val localEntity = userMapper.toLocal(domainUser)

                localDataSource.saveUser(localEntity)
                localDataSource.saveOnboardingStatus(domainUser.isOnboardingCompleted)
            }
        }
    }

    override fun observeUser(): Flow<User?> {
        return remoteDataSource.currentUserIdFlow.flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(null)
            } else {
                localDataSource.observeUserById(currentUserId).map { entity ->
                    entity?.let { userMapper.toDomain(it) }
                }
            }
        }
    }

    override suspend fun getUser(): User? {
        val userId = remoteDataSource.currentUserId ?: return null
        val localEntity = localDataSource.getUserById(userId)
        return localEntity?.let { userMapper.toDomain(it) }
    }

    override suspend fun getUserById(userId: String): User? {
        val localUsers = localDataSource.getUsersByIds(listOf(userId))
        val cachedUser = localUsers.firstOrNull()?.let { userMapper.toDomain(it) }

        if (cachedUser != null) {
            return cachedUser
        }

        val remoteUserDto = remoteDataSource.getUserProfile(userId) ?: return null
        val domainUser = userMapper.toDomain(remoteUserDto)
        val localEntity = userMapper.toLocal(domainUser)

        localDataSource.saveUser(localEntity)

        return domainUser
    }

    override suspend fun syncSession(): Result<Unit> {
        return runCatching {
            val userId = remoteDataSource.currentUserId
                ?: throw IllegalStateException("User not logged in")

            val remoteUserDto = remoteDataSource.getUserProfile(userId)

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                val localEntity = userMapper.toLocal(domainUser)

                localDataSource.saveUser(localEntity)
                localDataSource.saveOnboardingStatus(domainUser.isOnboardingCompleted)
            }
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return runCatching {
            val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
            val completedUser = user.copy(id = userId)

            val userDto = userMapper.toDto(completedUser)
            val localEntity = userMapper.toLocal(completedUser)

            remoteDataSource.saveUserProfile(userDto)
            localDataSource.saveUser(localEntity)
            localDataSource.saveOnboardingStatus(completedUser.isOnboardingCompleted)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return runCatching {
            val userId = remoteDataSource.currentUserId
                ?: throw IllegalStateException("No active user")
            val localUser = localDataSource.getUserById(userId)
            val currentEmail = localUser?.email.orEmpty()

            val completedUser = user.copy(
                id = userId,
                email = currentEmail
            )

            val userDto = userMapper.toDto(completedUser)
            val localEntity = userMapper.toLocal(completedUser)

            remoteDataSource.saveUserProfile(userDto)
            localDataSource.saveUser(localEntity)
            localDataSource.saveOnboardingStatus(completedUser.isOnboardingCompleted)
        }
    }

    override suspend fun logOut(): Result<Unit> {
        return runCatching {
            remoteDataSource.signOut()
            localDataSource.clearLocalData()
        }
    }

    override suspend fun checkIsOnboardingCompleted(): Boolean {
        val userId = remoteDataSource.currentUserId ?: return false
        val isCompleted = localDataSource.getUserById(userId)?.isOnboardingCompleted ?: false
        return isCompleted
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = runCatching {
        val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
        remoteDataSource.updateFcmToken(userId, token)
    }
}