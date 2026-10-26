package com.furiqd.local.datasource

import com.furiqd.local.contract.RoomLocalDataSource
import com.furiqd.local.entity.DailyChallengeEntity
import com.furiqd.local.entity.RoomEntity
import com.furiqd.local.room.DailyChallengeDao
import com.furiqd.local.room.RoomDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomLocalDataSourceImpl@Inject constructor(
    private val roomDao: RoomDao,
    private val challengeDao: DailyChallengeDao
): RoomLocalDataSource {
    override fun observeUserRooms(userId: String): Flow<List<RoomEntity>> {
        return roomDao.observeUserRooms(userId)
    }

    override suspend fun insertRooms(rooms: List<RoomEntity>) {
        return roomDao.insertRooms(rooms)
    }

    override suspend fun getChallenge(challengeId: String): DailyChallengeEntity? {
        return challengeDao.getChallenge(challengeId)
    }

    override suspend fun insertChallenge(challenge: DailyChallengeEntity) {
        return challengeDao.insertChallenge(challenge)
    }

    override suspend fun deleteChallenge(challengeId: String) {
        return challengeDao.deleteChallenge(challengeId)
    }

    override suspend fun deleteAllChallenges() {
        return challengeDao.deleteAllChallenges()
    }
}