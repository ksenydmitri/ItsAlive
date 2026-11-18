package com.furiqd.network.datasource

import com.furiqd.network.dto.AuthResultDto
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthWithFirebaseAuthenticator @Inject constructor(
    val auth: FirebaseAuth
) {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            checkNotNull(result.user) { "Firebase user is null" }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> =
        registerOrReconcile(
            createAccount = {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                checkNotNull(result.user) { "Firebase user is null" }
            },
            confirmExistingAccount = {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                checkNotNull(result.user) { "Firebase user is null" }
            }
        )

    suspend fun signInWithGoogleToken(idToken: String): Result<AuthResultDto> {
        return try {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(authCredential).await()

            val firebaseUser = result.user ?: throw Exception("Firebase user is null")

            val authResult = AuthResultDto(
                email = firebaseUser.email ?: throw Exception("Google account missing email"),
                name = firebaseUser.displayName
            )

            Result.success(authResult)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

internal suspend fun registerOrReconcile(
    createAccount: suspend () -> Unit,
    confirmExistingAccount: suspend () -> Unit,
    isUncertainOutcome: (Throwable) -> Boolean = { it.isRegistrationOutcomeUncertain() }
): Result<Unit> {
    try {
        createAccount()
        return Result.success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (registrationError: Exception) {
        if (!isUncertainOutcome(registrationError)) {
            return Result.failure(registrationError)
        }

        return try {
            confirmExistingAccount()
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (confirmationError: Exception) {
            if (registrationError !== confirmationError) {
                registrationError.addSuppressed(confirmationError)
            }
            Result.failure(registrationError)
        }
    }
}

private fun Throwable.isRegistrationOutcomeUncertain(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is FirebaseNetworkException || cause is FirebaseAuthUserCollisionException) {
            return true
        }
        val next = cause.cause
        if (next === cause) break
        cause = next
    }
    return false
}