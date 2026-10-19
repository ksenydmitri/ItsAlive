package com.furiqd.shared.data.mapper

import com.furiqd.local.entity.LocalUserEntity
import com.furiqd.network.dto.UserDto
import com.furiqd.shared.domain.model.User
import javax.inject.Inject

class UserDataMapper @Inject constructor(
    private val imageUrlFormatter: ImageUrlFormatter
) {
    fun toDomain(dto: UserDto): User {
        return User(
            id = dto.id,
            name = dto.name,
            avatarId = imageUrlFormatter.format(dto.avatarId),
            isOnboardingCompleted = dto.isOnboardingCompleted,
            roomIds = dto.roomIds,
            email = dto.email,
            nickName = dto.nickName
        )
    }

    fun toDomain(entity: LocalUserEntity): User {
        return User(
            id = entity.id,
            name = entity.name,
            avatarId = entity.avatarId,
            isOnboardingCompleted = entity.isOnboardingCompleted,
            roomIds = RoomConverters().toStringList(entity.roomIds) ?: emptyList(),
            email = entity.email,
            nickName = entity.nickName
        )
    }

    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            name = user.name,
            avatarId = user.avatarId,
            isOnboardingCompleted = user.isOnboardingCompleted,
            roomIds = user.roomIds,
            email = user.email,
            nickName = user.nickName
        )
    }

    fun toLocal(user: User): LocalUserEntity {
        return LocalUserEntity(
            id = user.id,
            name = user.name,
            avatarId = user.avatarId,
            isOnboardingCompleted = user.isOnboardingCompleted,
            roomIds = RoomConverters().fromStringList(user.roomIds),
            email = user.email,
            nickName = user.nickName
        )
    }
}