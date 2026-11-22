package com.furiqd.shared.data.repositories

import com.furiqd.local.contract.TokenLocalDataSource
import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.local.entity.LocalUserEntity
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.dto.UserDto
import com.furiqd.shared.data.mapper.ImageUrlFormatter
import com.furiqd.shared.data.mapper.UserDataMapper
import com.furiqd.shared.domain.model.UserSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositorySessionTest {

    @Test
    fun sessionStateReturnsNotAuthorizedWhenNoUserIdPresent() = runBlocking {
        val remote = SessionFakeRemoteAuthDataSource(initialUserId = null)
        val local = SessionFakeUserLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createSessionRepository(remote, local, scope)
            val state = repository.sessionState.first()
            assertEquals(UserSessionState.NotAuthorized, state)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun sessionStateReturnsFullyAuthorizedWhenOnboardingCompleted() = runBlocking {
        val remote = SessionFakeRemoteAuthDataSource(initialUserId = "uid-1")
        val local = SessionFakeUserLocalDataSource().apply {
            users["uid-1"] = LocalUserEntity(
                id = "uid-1",
                name = "Alice",
                avatarId = null,
                isOnboardingCompleted = true,
                roomIds = null,
                email = "alice@example.com",
                nickName = "alice"
            )
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createSessionRepository(remote, local, scope)
            val state = repository.sessionState.first()
            assertEquals(UserSessionState.FullyAuthorized, state)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun sessionStateReturnsNeedsOnboardingWhenOnboardingNotCompleted() = runBlocking {
        val remote = SessionFakeRemoteAuthDataSource(initialUserId = "uid-1")
        val local = SessionFakeUserLocalDataSource().apply {
            users["uid-1"] = LocalUserEntity(
                id = "uid-1",
                name = "Bob",
                avatarId = null,
                isOnboardingCompleted = false,
                roomIds = null,
                email = "bob@example.com",
                nickName = "bob"
            )
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createSessionRepository(remote, local, scope)
            val state = repository.sessionState.first()
            assertEquals(UserSessionState.NeedsOnboarding, state)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun syncSessionUpdatesLocalDataFromRemote() = runBlocking {
        val remote = SessionFakeRemoteAuthDataSource(
            initialUserId = "uid-1",
            remoteProfile = UserDto(
                id = "uid-1",
                name = "Remote Alice",
                isOnboardingCompleted = true,
                email = "alice@example.com"
            )
        )
        val local = SessionFakeUserLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createSessionRepository(remote, local, scope)
            val syncResult = repository.syncSession()
            assertTrue(syncResult.isSuccess)
            assertEquals("Remote Alice", local.users["uid-1"]?.name)
        } finally {
            scope.cancel()
        }
    }

    private fun createSessionRepository(
        remote: RemoteAuthDataSource,
        local: UserLocalDataSource,
        scope: CoroutineScope
    ): UserRepositoryImpl {
        val mapper = UserDataMapper(ImageUrlFormatter("https://example.com"))
        val tokenLocal = SessionFakeTokenLocalDataSource()
        return UserRepositoryImpl(remote, local, tokenLocal, mapper, scope)
    }
}

private class SessionFakeRemoteAuthDataSource(
    initialUserId: String? = null,
    val remoteProfile: UserDto? = null
) : RemoteAuthDataSource {
    val userIdState = MutableStateFlow(initialUserId)
    override val currentUserIdFlow: Flow<String?> = userIdState
    override val currentUserId: String? get() = userIdState.value

    override suspend fun getUserProfile(userId: String): UserDto? = remoteProfile
    override suspend fun saveUserProfile(userDto: UserDto) {}
    override suspend fun checkOnboardingStatus(userId: String): Boolean = false
    override suspend fun saveOnboardingStatus(userId: String, isCompleted: Boolean): Boolean = true
    override suspend fun signOut() { userIdState.value = null }
    override suspend fun updateFcmToken(userId: String, token: String) {}
}

private class SessionFakeUserLocalDataSource : UserLocalDataSource {
    val users = mutableMapOf<String, LocalUserEntity>()
    override val isOnboardingCompletedFlow: Flow<Boolean> = emptyFlow()
    override suspend fun getOnboardingStatus(): Boolean = false
    override suspend fun saveOnboardingStatus(isCompleted: Boolean) {}
    override fun observeUserById(userId: String): Flow<LocalUserEntity?> = MutableStateFlow(users[userId])
    override suspend fun getUserById(userId: String): LocalUserEntity? = users[userId]
    override suspend fun saveUser(user: LocalUserEntity) { users[user.id] = user }
    override suspend fun clearLocalData(userId: String?) {
        if (userId != null) users.remove(userId) else users.clear()
    }
    override suspend fun getUsersByIds(ids: List<String>): List<LocalUserEntity> = ids.mapNotNull { users[it] }
}

private class SessionFakeTokenLocalDataSource : TokenLocalDataSource {
    override fun observeToken(): Flow<String?> = emptyFlow()
    override suspend fun getToken(): String? = null
    override suspend fun saveToken(token: String) {}
    override suspend fun clearToken() {}
}
