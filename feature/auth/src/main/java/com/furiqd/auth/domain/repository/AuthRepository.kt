package com.furiqd.auth.domain.repository

import com.furiqd.shared.domain.model.AuthResult

internal interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<AuthResult>
}
