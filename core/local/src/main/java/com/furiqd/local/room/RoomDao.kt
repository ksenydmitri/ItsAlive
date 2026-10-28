package com.furiqd.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.furiqd.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("""
        SELECT rooms.* FROM rooms 
        LEFT JOIN daily_challenges ON rooms.challengeId = daily_challenges.id 
        WHERE rooms.hostId = :userId OR rooms.friendId = :userId
    """)
    fun observeUserRooms(userId: String): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRooms(rooms: List<RoomEntity>)

    @Query("UPDATE rooms SET streakCount = :streakCount, isStreakActive = :isStreakActive WHERE id = :roomId")
    suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean)

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun observeRoomById(roomId: String): Flow<RoomEntity?>
}