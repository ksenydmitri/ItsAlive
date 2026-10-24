package com.furiqd.shared.domain.usecase

import android.util.Log
import com.furiqd.shared.domain.repository.UserRepository
import javax.inject.Inject

class SyncFcmTokenUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String) {
        Log.d("SyncFcmTokenUseCase", "Token: $token")
        userRepository.updateFcmToken(token)
    }
}