package com.furiqd.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.furiqd.local.entity.DailyChallengeEntity


@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE id = :challengeId")
    suspend fun getChallenge(challengeId: String): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallengeEntity)

    @Query("DELETE FROM daily_challenges WHERE id = :challengeId")
    suspend fun deleteChallenge(challengeId: String)

    @Query("DELETE FROM daily_challenges")
    suspend fun deleteAllChallenges()
}