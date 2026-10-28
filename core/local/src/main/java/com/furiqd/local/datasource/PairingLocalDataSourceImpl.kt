package com.furiqd.local.datasource

import com.furiqd.local.contract.PairingLocalDataSource
import com.furiqd.local.entity.JointPetEntity
import com.furiqd.local.room.PetDao
import com.furiqd.local.storage.PairingPreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PairingLocalDataSourceImpl @Inject constructor(
    private val preferencesManager: PairingPreferencesManager,
    private val petDao: PetDao
) : PairingLocalDataSource {

    override fun getRoomId(): Flow<String?> = preferencesManager.roomId

    override fun isPairingComplete(): Flow<Boolean> = preferencesManager.isPairingComplete

    override suspend fun saveRoomId(roomId: String) {
        preferencesManager.saveRoomId(roomId)
    }

    override suspend fun setPairingComplete(isComplete: Boolean) {
        preferencesManager.setPairingComplete(isComplete)
    }

    override suspend fun clearLocalSession() {
        preferencesManager.clear()
        petDao.deletePet()
    }
}