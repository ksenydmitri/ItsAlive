package com.furiqd.home.domain.usecase

import com.furiqd.shared.domain.repository.UserRepository
import javax.inject.Inject

class SyncFcmTokenUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        userRepository.updateFcmToken(userId)
    }
}