package com.furiqd.home.domain.repository

import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.PairingRoom
import kotlinx.coroutines.flow.Flow

internal interface HomeRepository {
    fun observeUserRooms(userId: String): Flow<List<PairingRoom>>
    suspend fun deleteRoom(roomId: String)
    fun observeChallenge(challengeId: String): Flow<DailyChallenge>
    suspend fun getChallenge(challengeId: String): Result<DailyChallenge>

    suspend fun submitChallengePhoto(
        roomId: String,
        photoPath: String,
        userId: String
    ): Result<Unit>

    suspend fun checkActiveHour(challengeId: String): Boolean
    suspend fun getRoomWithChallenge(roomId: String, userId: String): Result<PairingRoom>
    suspend fun inciteFriend(roomId: String, senderId: String, recipientId: String): Result<Unit>
}