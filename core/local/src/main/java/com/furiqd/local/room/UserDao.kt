package com.furiqd.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.furiqd.local.entity.LocalUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<LocalUserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): LocalUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<LocalUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<LocalUserEntity>)
}