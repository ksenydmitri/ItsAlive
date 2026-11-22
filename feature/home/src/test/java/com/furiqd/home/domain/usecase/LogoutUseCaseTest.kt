package com.furiqd.home.domain.usecase

import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutUseCaseTest {
    @Test
    fun invokeReturnsRepositorySuccessResult() = runBlocking {
        val repositoryResult = Result.success(Unit)

        val result = LogoutUseCase(FakeUserRepository(repositoryResult))()

        assertTrue(result.isSuccess)
    }

    @Test
    fun invokeReturnsRepositoryFailureResultWithoutConvertingItToSuccess() = runBlocking {
        val repositoryResult = Result.failure<Unit>(IllegalStateException("Logout failed"))

        val result = LogoutUseCase(FakeUserRepository(repositoryResult))()

        assertTrue(result.isFailure)
        assertSame(repositoryResult.exceptionOrNull(), result.exceptionOrNull())
    }
}

private class FakeUserRepository(
    private val logoutResult: Result<Unit>
) : UserRepository {
    override val sessionState = MutableStateFlow<UserSessionState>(UserSessionState.NotAuthorized)

    override fun observeUser(): Flow<User?> = emptyFlow()

    override suspend fun getUser(): User? = null

    override suspend fun syncSession(): Result<Unit> = unsupportedOperation()

    override suspend fun saveUser(user: User): Result<Unit> = unsupportedOperation()

    override suspend fun logOut(): Result<Unit> = logoutResult

    override suspend fun checkIsOnboardingCompleted(): Boolean = false

    override suspend fun updateUser(user: User): Result<Unit> = unsupportedOperation()

    override suspend fun getUserById(userId: String): User? = null

    override suspend fun updateFcmToken(token: String): Result<Unit> = unsupportedOperation()

    private fun unsupportedOperation(): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}
