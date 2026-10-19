package com.furiqd.local.contract

import com.furiqd.local.entity.DailyChallengeEntity
import kotlinx.coroutines.flow.Flow

interface ChallengeLocalDataSource {
    suspend fun saveChallenge(challenge: DailyChallengeEntity)
    suspend fun getCachedChallenge(challengeId: String): DailyChallengeEntity?
    fun observeCachedChallenge(challengeId: String): Flow<DailyChallengeEntity?>
    suspend fun clearCache()
}