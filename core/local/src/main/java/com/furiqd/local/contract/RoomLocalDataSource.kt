package com.furiqd.local.contract

import com.furiqd.local.entity.DailyChallengeEntity
import com.furiqd.local.entity.RoomEntity
import com.furiqd.local.entity.RoomWithChallengeEntity
import kotlinx.coroutines.flow.Flow

interface RoomLocalDataSource {
    fun observeUserRooms(userId: String): Flow<List<RoomWithChallengeEntity>>

    suspend fun getRoomWithChallenge(roomId: String): RoomWithChallengeEntity?
    suspend fun insertRooms(rooms: List<RoomEntity>)
    suspend fun insertChallenge(challenge: DailyChallengeEntity)
    suspend fun deleteChallenge(challengeId: String)
    suspend fun deleteAllChallenges()
    suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean)
}