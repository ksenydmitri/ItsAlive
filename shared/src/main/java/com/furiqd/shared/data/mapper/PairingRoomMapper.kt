package com.furiqd.shared.data.mapper

import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.network.dto.PairingRoomDto
import com.furiqd.shared.domain.model.ChallengeTask
import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.RoomStatus
import com.google.firebase.Timestamp
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
        val domainStatus = when (dto.status.uppercase()) {
            "WAITING" -> RoomStatus.WaitingForFriend
            "ACTIVE" -> RoomStatus.Active
            else -> RoomStatus.NotFound
        }

        return PairingRoom(
            id = dto.id,
            code = dto.code,
            hostId = dto.hostId,
            friendId = dto.friendId,
            pet = dto.pet.toDomain(),
            createdAt = dto.createdAt,
            status = domainStatus,
            challenge = challengeDto?.toDomain(),
            isStreakActive = dto.isStreakActive,
            streakCount = dto.streakCount
        )
    }

    fun toDto(domain: PairingRoom): PairingRoomDto {
        val stringStatus = when (domain.status) {
            is RoomStatus.WaitingForFriend -> "WAITING"
            is RoomStatus.Active -> "ACTIVE"
            is RoomStatus.NotFound -> "NOT_FOUND"
        }

        return PairingRoomDto(
            id = domain.id,
            code = domain.code,
            hostId = domain.hostId,
            friendId = domain.friendId,
            pet = domain.pet.toDto(),
            createdAt = domain.createdAt,
            status = stringStatus,
            challengeId = domain.challenge?.id
        )
    }

    private fun DailyChallengeDto.toDomain(): DailyChallenge {
        return DailyChallenge(
            id = id,
            isActiveHour = isActiveHour,
            expiresAt = expiresAt,
            task = task?.toDomain(),
            userPhotoUrl = imageUrlFormatter.format(userPhotoUrl),
            friendPhotoUrl = imageUrlFormatter.format(friendPhotoUrl),
            compositeImageUrl = imageUrlFormatter.format(compositeImageUrl)
        )
    }

    private fun ChallengeTaskDto.toDomain(): ChallengeTask {
        return ChallengeTask(
            userTaskPrompt = userTaskPrompt,
            friendTaskPrompt = friendTaskPrompt
        )
    }
}