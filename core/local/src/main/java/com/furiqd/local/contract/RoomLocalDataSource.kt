package com.furiqd.local.contract

import com.furiqd.local.entity.DailyChallengeEntity
import com.furiqd.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

interface RoomLocalDataSource {
    fun observeUserRooms(userId: String): Flow<List<RoomEntity>>
    suspend fun insertRooms(rooms: List<RoomEntity>)
    suspend fun getChallenge(challengeId: String): DailyChallengeEntity?
    suspend fun insertChallenge(challenge: DailyChallengeEntity)
    suspend fun deleteChallenge(challengeId: String)
    suspend fun deleteAllChallenges()
    suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean)
}