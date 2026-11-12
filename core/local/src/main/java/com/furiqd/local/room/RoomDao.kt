package com.furiqd.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.furiqd.local.entity.RoomEntity
import com.furiqd.local.entity.RoomWithChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Transaction
    @Query("""
    SELECT * FROM rooms 
    WHERE hostId = :userId OR friendId = :userId
    ORDER BY createdAt DESC
    """)
    fun observeUserRoomsWithChallenge(userId: String): Flow<List<RoomWithChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRooms(rooms: List<RoomEntity>)

    @Query("UPDATE rooms SET streakCount = :streakCount, isStreakActive = :isStreakActive WHERE id = :roomId")
    suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean)

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun observeRoomById(roomId: String): Flow<RoomEntity?>

    @Transaction
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomWithChallenge(roomId: String): RoomWithChallengeEntity?
}