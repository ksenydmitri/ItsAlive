package com.furiqd.home.presentation.mapper

import com.furiqd.home.domain.model.HomeRoomDetails
import com.furiqd.home.presentation.model.RoomUiModel
import com.furiqd.shared.config.PetEggConfig
import com.furiqd.shared.domain.model.RoomStatus

fun HomeRoomDetails.toUiModel(isSelected: Boolean = false): RoomUiModel {
    return when (room.status) {
        RoomStatus.WaitingForFriend -> {
            RoomUiModel.WaitingForFriend(
                id = room.id,
                isSelected = isSelected,
                inviteCode = room.code,
                petName = room.pet.name,
                petAvatarId = PetEggConfig.getDrawableByType(room.pet.petEggType),
                expiringTime = room.expirationTime,
                userAvatarUrl = currentUser.avatarId
            )
        }

        RoomStatus.Active -> {
            val challenge = room.challenge
            val challengeUiModel = if (challenge != null && challenge.isActiveHour) {
                challenge.toUiModel(
                    isCurrentUserHost = this.isHost
                )
            } else {
                null
            }

            RoomUiModel.Active(
                id = room.id,
                isSelected = isSelected,
                petName = room.pet.name,
                petAvatarId = PetEggConfig.getDrawableByType(room.pet.petEggType),
                userAvatarUrl = currentUser.avatarId,
                friendAvatarUrl = partnerUser?.avatarId,
                isStreakActive = room.isStreakActive,
                streakCount = room.streakCount,
                isActiveHour = challenge?.isActiveHour ?: false,
                challengeUiModel = challengeUiModel
            )
        }

        else -> RoomUiModel.Placeholder(id = room.id, isSelected = isSelected)
    }
}