package com.furiqd.shared.data.mapper

import com.furiqd.local.entity.DailyChallengeEntity
import com.furiqd.local.entity.RoomEntity
import com.furiqd.local.entity.RoomPetEmbedded
import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.network.dto.JointPetDto
import com.furiqd.network.dto.PairingRoomDto
import com.furiqd.shared.domain.model.ChallengeTask
import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.JointPet
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.PetEggType
import com.furiqd.shared.domain.model.RoomStatus
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMapper @Inject constructor(
    private val imageUrlFormatter: ImageUrlFormatter
) {
    fun toDomain(
        dto: PairingRoomDto,
        challengeDto: DailyChallengeDto? = null
    ): PairingRoom {
        return PairingRoom(
            id = dto.id,
            code = dto.code,
            hostId = dto.hostId,
            friendId = dto.friendId,
            pet = dto.pet.toDomain(),
            createdAt = dto.createdAt,
            status = dto.status.toRoomStatus(),
            challenge = challengeDto?.toDomain(),
            isStreakActive = dto.isStreakActive,
            streakCount = dto.streakCount
        )
    }

    fun toDto(domain: PairingRoom): PairingRoomDto {
        return PairingRoomDto(
            id = domain.id,
            code = domain.code,
            hostId = domain.hostId,
            friendId = domain.friendId,
            pet = domain.pet.toDto(),
            createdAt = domain.createdAt,
            status = domain.status.toStatusString(),
            challengeId = domain.challenge?.id
        )
    }

    fun toEntity(dto: PairingRoomDto): RoomEntity {
        return RoomEntity(
            id = dto.id,
            code = dto.code,
            hostId = dto.hostId,
            friendId = dto.friendId,
            pet = dto.pet.toEmbedded(),
            createdAt = dto.createdAt,
            status = dto.status,
            challengeId = dto.challengeId,
            isStreakActive = dto.isStreakActive,
            streakCount = dto.streakCount
        )
    }

    fun toDomain(
        entity: RoomEntity,
        challengeEntity: DailyChallengeEntity? = null
    ): PairingRoom {
        return PairingRoom(
            id = entity.id,
            code = entity.code,
            hostId = entity.hostId,
            friendId = entity.friendId,
            pet = entity.pet.toDomain(),
            createdAt = entity.createdAt,
            status = entity.status.toRoomStatus(),
            challenge = challengeEntity?.let { toDomain(it) },
            isStreakActive = entity.isStreakActive,
            streakCount = entity.streakCount
        )
    }

    fun toEntity(dto: DailyChallengeDto): DailyChallengeEntity {
        return DailyChallengeEntity(
            id = dto.id,
            isStreakActive = true,
            streakCount = 0,
            isActiveHour = dto.isActiveHour,
            expiresAt = dto.expiresAt,
            userTaskPrompt = dto.task?.userTaskPrompt,
            friendTaskPrompt = dto.task?.friendTaskPrompt,
            userPhotoUrl = dto.userPhotoUrl,
            friendPhotoUrl = dto.friendPhotoUrl,
            compositeImageUrl = dto.compositeImageUrl
        )
    }

    fun toDomain(entity: DailyChallengeEntity): DailyChallenge {
        val task = if (entity.userTaskPrompt != null || entity.friendTaskPrompt != null) {
            ChallengeTask(
                userTaskPrompt = entity.userTaskPrompt.orEmpty(),
                friendTaskPrompt = entity.friendTaskPrompt.orEmpty()
            )
        } else null

        return DailyChallenge(
            id = entity.id,
            isStreakActive = entity.isStreakActive,
            streakCount = entity.streakCount,
            isActiveHour = entity.isActiveHour,
            expiresAt = entity.expiresAt,
            task = task,
            userPhotoUrl = imageUrlFormatter.format(entity.userPhotoUrl),
            friendPhotoUrl = imageUrlFormatter.format(entity.friendPhotoUrl),
            compositeImageUrl = imageUrlFormatter.format(entity.compositeImageUrl)
        )
    }

    private fun String.toRoomStatus(): RoomStatus = when (this.uppercase()) {
        "WAITING" -> RoomStatus.WaitingForFriend
        "ACTIVE" -> RoomStatus.Active
        else -> RoomStatus.NotFound
    }

    private fun RoomStatus.toStatusString(): String = when (this) {
        is RoomStatus.WaitingForFriend -> "WAITING"
        is RoomStatus.Active -> "ACTIVE"
        is RoomStatus.NotFound -> "NOT_FOUND"
    }

    private fun DailyChallengeDto.toDomain(): DailyChallenge = DailyChallenge(
        id = id,
        isActiveHour = isActiveHour,
        expiresAt = expiresAt,
        task = task?.toDomain(),
        userPhotoUrl = imageUrlFormatter.format(userPhotoUrl),
        friendPhotoUrl = imageUrlFormatter.format(friendPhotoUrl),
        compositeImageUrl = imageUrlFormatter.format(compositeImageUrl)
    )

    private fun ChallengeTaskDto.toDomain(): ChallengeTask = ChallengeTask(
        userTaskPrompt = userTaskPrompt,
        friendTaskPrompt = friendTaskPrompt
    )
}