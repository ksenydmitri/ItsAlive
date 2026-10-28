package com.furiqd.local.contract

import com.furiqd.local.entity.JointPetEntity
import kotlinx.coroutines.flow.Flow

interface PairingLocalDataSource {
    fun getRoomId(): Flow<String?>
    fun isPairingComplete(): Flow<Boolean>
    suspend fun saveRoomId(roomId: String)
    suspend fun setPairingComplete(isComplete: Boolean)
    suspend fun clearLocalSession()
}