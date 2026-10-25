package com.furiqd.network.contract

import com.furiqd.network.dto.PairingRoomDto
import kotlinx.coroutines.flow.Flow

interface RemoteRoomDataSource {
    fun observeRoom(roomId: String): Flow<PairingRoomDto?>
    suspend fun getRoomDetails(roomId: String): PairingRoomDto?
    fun observeUserRooms(userId: String): Flow<List<PairingRoomDto>>
    suspend fun deleteRoom(roomId: String)
    suspend fun updateRoomChallengeId(roomId: String, challengeId: String)
    suspend fun createRoom(newRoomDto: PairingRoomDto): String
    suspend fun joinRoom(inviteCode: String, friendUserId: String): String
    suspend fun isCodeExisting(code: String): Boolean
    suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean)
}