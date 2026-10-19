package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.shared.domain.model.DailyChallenge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ObserveChallengeUseCase @Inject constructor(
    private val challengeRepository: HomeRepository
) {
    operator fun invoke(challengeId: String): Flow<DailyChallenge> =
        challengeRepository.observeChallenge(challengeId)
}