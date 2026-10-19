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
            Log.d(TAG, "sessionState: Auth userId changed = $userId")
            if (userId == null) {
                Log.d(TAG, "sessionState: No active user, emitting NotAuthorized")
                flowOf(UserSessionState.NotAuthorized)
            } else {
                syncSessionSilently(userId)
                localDataSource.observeUser().map { localEntity ->
                    val user = localEntity?.let { userMapper.toDomain(it) }
                    Log.d(TAG, "sessionState: Local entity mapped to Domain = $user")

                    val state = when {
                        user == null -> UserSessionState.Loading
                        user.isOnboardingCompleted -> UserSessionState.FullyAuthorized
                        else -> UserSessionState.NeedsOnboarding
                    }
                    Log.d(TAG, "sessionState: Calculated state = $state")
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
            Log.d(TAG, "syncSessionSilently: Fetching remote profile for $userId...")
            val remoteUserDto = remoteDataSource.getUserProfile(userId)

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                val localEntity = userMapper.toLocal(domainUser)

                Log.d(TAG, "syncSessionSilently: Saving fetched user to Room DB")
                localDataSource.saveUser(localEntity)
                localDataSource.saveOnboardingStatus(domainUser.isOnboardingCompleted)
            } else {
                Log.w(TAG, "syncSessionSilently: User profile not found on remote (New user)")
            }
        }.onFailure { e ->
            Log.e(TAG, "syncSessionSilently: Error syncing session", e)
        }
    }

    override fun observeUser(): Flow<User?> {
        return localDataSource.observeUser()
            .map { entity ->
                val domainUser = entity?.let { userMapper.toDomain(it) }
                Log.d(TAG, "observeUser: Mapped LocalEntity -> Domain User: avatarId=${domainUser?.avatarId}")
                domainUser
            }
            .onEach { user ->
                Log.d(TAG, "observeUser: Emitting user = $user")
            }
    }

    override suspend fun getUser(): User? {
        Log.d(TAG, "getUser: Fetching local user...")
        val localEntity = localDataSource.getUser()
        val domainUser = localEntity?.let { userMapper.toDomain(it) }
        Log.d(TAG, "getUser: Local entity = $localEntity -> Mapped Domain User = $domainUser")
        return domainUser
    }

    override suspend fun getUserById(userId: String): User? {
        Log.d(TAG, "getUserById: Fetching remote profile for userId = $userId")
        val remoteUserDto = remoteDataSource.getUserProfile(userId)
        if (remoteUserDto == null) {
            Log.w(TAG, "getUserById: Remote profile is null for userId = $userId")
            return null
        }
        val domainUser = userMapper.toDomain(remoteUserDto)
        Log.d(TAG, "getUserById: Remote DTO mapped to Domain User = $domainUser (avatarUrl=${domainUser.avatarId})")
        return domainUser
    }

    override suspend fun syncSession(): Result<Unit> {
        Log.d(TAG, "syncSession: Starting session sync...")
        return runCatching {
            val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("User not logged in")
            Log.d(TAG, "syncSession: Current userId = $userId. Fetching remote profile...")

            val remoteUserDto = remoteDataSource.getUserProfile(userId)
            Log.d(TAG, "syncSession: Received remote DTO = $remoteUserDto")

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                val localEntity = userMapper.toLocal(domainUser)

                Log.d(TAG, "syncSession: Mapped DTO -> Domain: avatarUrl=${domainUser.avatarId}")
                Log.d(TAG, "syncSession: Saving mapped LocalEntity to DB = $localEntity")

                localDataSource.saveUser(localEntity)
                localDataSource.saveOnboardingStatus(domainUser.isOnboardingCompleted)
            } else {
                Log.w(TAG, "syncSession: Remote user DTO was null for userId = $userId")
                Unit
            }
        }.onFailure { throwable ->
            Log.e(TAG, "syncSession: Failed with exception", throwable)
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        Log.d(TAG, "saveUser: Input User = $user")
        return runCatching {
            val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
            val completedUser = user.copy(id = userId)

            val userDto = userMapper.toDto(completedUser)
            val localEntity = userMapper.toLocal(completedUser)

            Log.d(TAG, "saveUser: Sending DTO to remote = $userDto")
            remoteDataSource.saveUserProfile(userDto)

            Log.d(TAG, "saveUser: Saving LocalEntity to DB = $localEntity")
            localDataSource.saveUser(localEntity)
            localDataSource.saveOnboardingStatus(completedUser.isOnboardingCompleted)
        }.onFailure { throwable ->
            Log.e(TAG, "saveUser: Failed to save user", throwable)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        Log.d(TAG, "updateUser: Input User = $user")
        return runCatching {
            val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
            val localUser = localDataSource.getUser()
            val currentEmail = localUser?.email.orEmpty()

            val completedUser = user.copy(
                id = userId,
                email = currentEmail
            )

            val userDto = userMapper.toDto(completedUser)
            val localEntity = userMapper.toLocal(completedUser)

            Log.d(TAG, "updateUser: Updating Remote DTO = $userDto")
            remoteDataSource.saveUserProfile(userDto)

            Log.d(TAG, "updateUser: Updating LocalEntity = $localEntity")
            localDataSource.saveUser(localEntity)
            localDataSource.saveOnboardingStatus(completedUser.isOnboardingCompleted)
            Log.d(TAG, "updateUser: User updated successfully")
            Unit
        }.onFailure { throwable ->
            Log.e(TAG, "updateUser: Failed to update user", throwable)
        }
    }

    override suspend fun logOut(): Result<Unit> {
        Log.d(TAG, "logOut: Logging out user...")
        return runCatching {
            remoteDataSource.signOut()
            localDataSource.clearLocalData()
            Log.d(TAG, "logOut: User logged out successfully")
            Unit
        }.onFailure { throwable ->
            Log.e(TAG, "logOut: Failed to log out", throwable)
        }
    }

    override suspend fun checkIsOnboardingCompleted(): Boolean {
        val isCompleted = localDataSource.getUser()?.isOnboardingCompleted ?: false
        Log.d(TAG, "checkIsOnboardingCompleted: $isCompleted")
        return isCompleted
    }
}