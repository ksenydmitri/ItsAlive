package com.furiqd.auth.data.repository

import com.furiqd.auth.domain.repository.AuthRepository
import com.furiqd.network.datasource.AuthWithFirebaseAuthenticator
import com.furiqd.shared.domain.model.AuthResult
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val authWithFirebaseAuthenticator: AuthWithFirebaseAuthenticator
) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> = authWithFirebaseAuthenticator.signInWithEmailAndPassword(email, password)

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Result<Unit> = authWithFirebaseAuthenticator.signUpWithEmailAndPassword(email, password)

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            val firebaseResult = authWithFirebaseAuthenticator.signInWithGoogleToken(idToken)
            firebaseResult.fold(
                onSuccess = { result ->
                    Result.success(
                        AuthResult(
                            email = result.email,
                            name = result.name
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}