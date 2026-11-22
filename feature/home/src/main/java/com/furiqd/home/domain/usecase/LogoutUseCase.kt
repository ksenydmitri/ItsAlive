package com.furiqd.home.domain.usecase

import com.furiqd.shared.domain.repository.UserRepository
import javax.inject.Inject

internal class LogoutUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = userRepository.logOut()
}