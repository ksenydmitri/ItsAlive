package com.furiqd.shared.data.repositories

import com.furiqd.local.contract.TokenLocalDataSource
import com.furiqd.local.contract.UserLocalDataSource
import com.furiqd.local.entity.LocalUserEntity
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.dto.UserDto
import com.furiqd.shared.data.mapper.ImageUrlFormatter
import com.furiqd.shared.data.mapper.UserDataMapper
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryLogoutTest {
    @Test
    fun localDataIsClearedWhenProviderSignOutFails() = runBlocking {
        val signOutError = IllegalStateException("sign out failed")
        val remote = FakeRemoteAuthDataSource(
            signOutError = signOutError,
            initialUserId = "uid-1"
        )
        val local = FakeUserLocalDataSource()
        val tokenLocal = FakeTokenLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val result = createRepository(remote, local, scope, tokenLocal).logOut()

            assertTrue(result.isFailure)
            assertSame(signOutError, result.exceptionOrNull())
            assertEquals(1, local.clearCalls)
            assertEquals(1, tokenLocal.clearCalls)
            assertEquals(listOf("uid-1"), local.clearedUserIds)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun repeatedLogoutClearsLocalDataAndSucceeds() = runBlocking {
        val local = FakeUserLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createRepository(FakeRemoteAuthDataSource(), local, scope)

            assertTrue(repository.logOut().isSuccess)
            assertTrue(repository.logOut().isSuccess)
            assertEquals(2, local.clearCalls)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun logoutClearsOnlyTheCurrentUsersCachedProfile() = runBlocking {
        val local = FakeUserLocalDataSource().apply {
            cachedUserIds.addAll(listOf("uid-1", "uid-2"))
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createRepository(
                FakeRemoteAuthDataSource(initialUserId = "uid-1"),
                local,
                scope
            )

            repository.logOut()

            assertEquals(setOf("uid-2"), local.cachedUserIds)
            assertEquals(listOf("uid-1"), local.clearedUserIds)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun localSessionIsUnauthenticatedImmediatelyWhenProviderSignOutFails() = runBlocking {
        val remote = FakeRemoteAuthDataSource(
            signOutError = IllegalStateException("sign out failed"),
            initialUserId = "uid-1"
        )
        val local = FakeUserLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createRepository(remote, local, scope)

            repository.logOut()

            assertEquals(UserSessionState.NotAuthorized, repository.sessionState.value)
            assertEquals(null, repository.getUser())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun cancellationDuringProviderSignOutStillClearsLocalData() = runBlocking {
        val cancellation = CancellationException("cancelled")
        val local = FakeUserLocalDataSource()
        val tokenLocal = FakeTokenLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createRepository(
                FakeRemoteAuthDataSource(signOutError = cancellation),
                local,
                scope,
                tokenLocal
            )

            var wasCancelled = false
            try {
                repository.logOut()
            } catch (error: CancellationException) {
                wasCancelled = true
                assertSame(cancellation, error)
            }

            assertTrue(wasCancelled)
            assertEquals(1, local.clearCalls)
            assertEquals(1, tokenLocal.clearCalls)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun profileSaveCompletingAfterLogoutDoesNotRestoreLocalUser() = runBlocking {
        val profileSaveGate = CompletableDeferred<Unit>()
        val remote = FakeRemoteAuthDataSource(
            initialUserId = "uid-1",
            profileSaveGate = profileSaveGate
        )
        val local = FakeUserLocalDataSource()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val repository = createRepository(remote, local, scope)
            val save = async(start = CoroutineStart.UNDISPATCHED) {
                repository.saveUser(
                    User(
                        id = "uid-1",
                        name = "User",
                        avatarId = null,
                        isOnboardingCompleted = false,
                        roomIds = emptyList(),
                        email = "person@example.com",
                        nickName = null
                    )
                )
            }

            repository.logOut()
            profileSaveGate.complete(Unit)
            save.await()

            assertEquals(0, local.saveCalls)
        } finally {
            scope.cancel()
        }
    }

    private fun createRepository(
        remote: FakeRemoteAuthDataSource,
        local: FakeUserLocalDataSource,
        scope: CoroutineScope,
        tokenLocal: FakeTokenLocalDataSource = FakeTokenLocalDataSource()
    ) = UserRepositoryImpl(
        remoteDataSource = remote,
        localDataSource = local,
        tokenLocalDataSource = tokenLocal,
        userMapper = UserDataMapper(ImageUrlFormatter("https://example.com")),
        scope = scope
    )
}

private class FakeRemoteAuthDataSource(
    private val signOutError: Exception? = null,
    initialUserId: String? = null,
    private val profileSaveGate: CompletableDeferred<Unit>? = null
) : RemoteAuthDataSource {
    private val currentUserIds = kotlinx.coroutines.flow.MutableStateFlow(initialUserId)
    override val currentUserIdFlow: Flow<String?> = currentUserIds
    override val currentUserId: String? get() = currentUserIds.value
    override suspend fun getUserProfile(userId: String): UserDto? = null
    override suspend fun saveUserProfile(userDto: UserDto) {
        profileSaveGate?.await()
    }
    override suspend fun checkOnboardingStatus(userId: String): Boolean = false
    override suspend fun updateFcmToken(userId: String, token: String) = Unit
    override suspend fun saveOnboardingStatus(userId: String, isCompleted: Boolean): Boolean = true
    override suspend fun signOut() {
        signOutError?.let { throw it }
    }
}

private class FakeUserLocalDataSource : UserLocalDataSource {
    var clearCalls = 0
        private set
    var saveCalls = 0
        private set
    val cachedUserIds = mutableSetOf<String>()
    val clearedUserIds = mutableListOf<String?>()

    override val isOnboardingCompletedFlow: Flow<Boolean> = emptyFlow()
    override suspend fun getOnboardingStatus(): Boolean = false
    override suspend fun saveOnboardingStatus(isCompleted: Boolean) = Unit
    override fun observeUserById(userId: String): Flow<LocalUserEntity?> = emptyFlow()
    override suspend fun getUserById(userId: String): LocalUserEntity? = null
    override suspend fun saveUser(user: LocalUserEntity) {
        saveCalls++
        cachedUserIds += user.id
    }
    override suspend fun clearLocalData(userId: String?) {
        clearCalls++
        clearedUserIds += userId
        if (userId == null) {
            cachedUserIds.clear()
        } else {
            cachedUserIds -= userId
        }
    }

    override suspend fun getUsersByIds(ids: List<String>): List<LocalUserEntity> = emptyList()
}

private class FakeTokenLocalDataSource : TokenLocalDataSource {
    var clearCalls = 0
        private set

    override fun observeToken(): Flow<String?> = emptyFlow()
    override suspend fun getToken(): String? = null
    override suspend fun saveToken(token: String) = Unit
    override suspend fun clearToken() {
        clearCalls++
    }
}
