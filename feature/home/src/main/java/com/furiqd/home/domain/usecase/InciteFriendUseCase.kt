package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.repository.HomeRepository
import javax.inject.Inject

internal class InciteFriendUseCase@Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(roomId: String): Result<Unit> = homeRepository.inciteFriend(roomId)
}