package com.furiqd.auth.testing

import com.furiqd.auth.domain.repository.AuthRepository
import com.furiqd.shared.domain.model.AuthResult
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeAuthRepository(
    var signInResult: suspend (String, String) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    var signUpResult: suspend (String, String) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    var googleSignInResult: suspend (String) -> Result<AuthResult> = {
        Result.failure(UnsupportedOperationException())
    }
) : AuthRepository {
    var signInCalls = 0
        private set
    var signUpCalls = 0
        private set
    var googleSignInCalls = 0
        private set

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> {
        signInCalls++
        return signInResult(email, password)
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> {
        signUpCalls++
        return signUpResult(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        googleSignInCalls++
        return googleSignInResult(idToken)
    }
}

internal class FakeUserRepository(
    var saveUserResult: Result<Unit> = Result.success(Unit),
    var syncSessionResult: Result<Unit> = Result.success(Unit),
    var currentUser: User? = null
) : UserRepository {
    override val sessionState = MutableStateFlow<UserSessionState>(UserSessionState.NotAuthorized)
    val savedUsers = mutableListOf<User>()
    val updatedUsers = mutableListOf<User>()

    override fun observeUser(): Flow<User?> = emptyFlow()
    override suspend fun getUser(): User? = currentUser
    override suspend fun syncSession(): Result<Unit> = syncSessionResult
    override suspend fun saveUser(user: User): Result<Unit> {
        savedUsers += user
        return saveUserResult
    }
    override suspend fun logOut(): Result<Unit> = Result.success(Unit)
    override suspend fun checkIsOnboardingCompleted(): Boolean =
        currentUser?.isOnboardingCompleted ?: false
    override suspend fun updateUser(user: User): Result<Unit> {
        updatedUsers += user
        return Result.success(Unit)
    }
    override suspend fun getUserById(userId: String): User? = null
    override suspend fun updateFcmToken(token: String): Result<Unit> = Result.success(Unit)
}
