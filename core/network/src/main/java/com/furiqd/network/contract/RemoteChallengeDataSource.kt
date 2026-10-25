package com.furiqd.network.contract

import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import kotlinx.coroutines.flow.Flow

interface RemoteChallengeDataSource {
    suspend fun getChallengeById(challengeId: String): DailyChallengeDto?
    suspend fun uploadUserPhoto(
        challengeId: String,
        userId: String,
        photoBytes: ByteArray,
        mimeType: String
    ): String
    suspend fun updatePhotoUrl(
        challengeId: String,
        isHost: Boolean,
        photoPath: String
    )

    suspend fun createChallenge(challengeDto: DailyChallengeDto): DailyChallengeDto
    suspend fun getRandomTask(): ChallengeTaskDto?
    suspend fun inciteFriend(
        roomId: String,
        senderId: String,
        recipientId: String
    )
}