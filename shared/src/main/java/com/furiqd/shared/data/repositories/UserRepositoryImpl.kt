package com.furiqd.shared.data.repositories

import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.local.contract.TokenLocalDataSource
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.shared.data.di.ApplicationScope
import com.furiqd.shared.data.mapper.UserDataMapper
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAuthDataSource,
    private val localDataSource: UserLocalDataSource,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val userMapper: UserDataMapper,
    @ApplicationScope private val scope: CoroutineScope
) : UserRepository {

    private val locallySignedOutUserId = MutableStateFlow<String?>(null)
    private val localAuthMutationMutex = Mutex()
    private val currentUserIdFlow = remoteDataSource.currentUserIdFlow.onEach { userId ->
        if (userId == null || userId == locallySignedOutUserId.value) {
            locallySignedOutUserId.value = null
        }
    }

    override val sessionState: StateFlow<UserSessionState> = combine(
        currentUserIdFlow,
        locallySignedOutUserId
    ) { userId, signedOutUserId ->
        userId to signedOutUserId
    }.flatMapLatest { (userId, signedOutUserId) ->
            if (userId == null || userId == signedOutUserId) {
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
        try {
            val remoteUserDto = remoteDataSource.getUserProfile(userId)

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                persistSessionProfile(userId, domainUser)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Profile load failures do not establish or remove provider authentication.
        }
    }

    override fun observeUser(): Flow<User?> {
        return combine(currentUserIdFlow, locallySignedOutUserId) { userId, signedOutUserId ->
            userId to signedOutUserId
        }.flatMapLatest { (currentUserId, signedOutUserId) ->
            if (currentUserId == null || currentUserId == signedOutUserId) {
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
        if (userId == locallySignedOutUserId.value) return null
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
        return resultOf {
            val userId = remoteDataSource.currentUserId
                ?: throw IllegalStateException("User not logged in")
            check(userId != locallySignedOutUserId.value) { "User is signed out locally" }

            val remoteUserDto = remoteDataSource.getUserProfile(userId)

            if (remoteUserDto != null) {
                val domainUser = userMapper.toDomain(remoteUserDto)
                persistSessionProfile(userId, domainUser)
            }
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return resultOf {
            val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
            check(userId != locallySignedOutUserId.value) { "User is signed out locally" }
            val completedUser = user.copy(id = userId)

            val userDto = userMapper.toDto(completedUser)

            remoteDataSource.saveUserProfile(userDto)
            persistSessionProfile(userId, completedUser)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return resultOf {
            val userId = remoteDataSource.currentUserId
                ?: throw IllegalStateException("No active user")
            check(userId != locallySignedOutUserId.value) { "User is signed out locally" }
            val localUser = localDataSource.getUserById(userId)
            val currentEmail = localUser?.email.orEmpty()

            val completedUser = user.copy(
                id = userId,
                email = currentEmail
            )

            val userDto = userMapper.toDto(completedUser)

            remoteDataSource.saveUserProfile(userDto)
            persistSessionProfile(userId, completedUser)
        }
    }

    override suspend fun logOut(): Result<Unit> {
        val userId = remoteDataSource.currentUserId
        userId?.let { locallySignedOutUserId.value = it }
        val signOutError = try {
            remoteDataSource.signOut()
            null
        } catch (cancellation: CancellationException) {
            try {
                withContext(NonCancellable) {
                    clearLocalAuthData(userId)?.let(cancellation::addSuppressed)
                }
            } catch (cleanupError: Exception) {
                cancellation.addSuppressed(cleanupError)
            }
            throw cancellation
        } catch (error: Exception) {
            error
        }

        val cleanupError = withContext(NonCancellable) { clearLocalAuthData(userId) }
        if (signOutError != null) {
            cleanupError?.let(signOutError::addSuppressed)
            return Result.failure(signOutError)
        }
        return cleanupError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun clearLocalAuthData(userId: String?): Exception? {
        return localAuthMutationMutex.withLock {
            var firstFailure: Exception? = null
            try {
                localDataSource.clearLocalData(userId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                firstFailure = error
            }

            try {
                tokenLocalDataSource.clearToken()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                if (firstFailure == null) {
                    firstFailure = error
                } else {
                    firstFailure?.addSuppressed(error)
                }
            }
            firstFailure
        }
    }

    private suspend fun persistSessionProfile(userId: String, user: User) {
        localAuthMutationMutex.withLock {
            check(
                remoteDataSource.currentUserId == userId &&
                    locallySignedOutUserId.value != userId
            ) { "Session changed before profile persistence" }

            localDataSource.saveUser(userMapper.toLocal(user))
            localDataSource.saveOnboardingStatus(user.isOnboardingCompleted)
        }
    }

    override suspend fun checkIsOnboardingCompleted(): Boolean {
        val userId = remoteDataSource.currentUserId ?: return false
        if (userId == locallySignedOutUserId.value) return false
        val isCompleted = localDataSource.getUserById(userId)?.isOnboardingCompleted ?: false
        return isCompleted
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = resultOf {
        val userId = remoteDataSource.currentUserId ?: throw IllegalStateException("No active user")
        check(userId != locallySignedOutUserId.value) { "User is signed out locally" }
        remoteDataSource.updateFcmToken(userId, token)
    }

    override suspend fun isNickNameAvailable(nickName: String): Boolean {
        return remoteDataSource.isNickNameAvailable(nickName)
    }

    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Result.failure(error)
    }
}