package com.furiqd.home.presentation.mapper

import com.furiqd.home.domain.model.HomeRoomDetails
import com.furiqd.home.presentation.screen.main.RoomUiModel
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
                expiringTime = room.createdAt,
                userAvatarUrl = currentUser.avatarId
            )
        }

        RoomStatus.Active -> {
            val challengeForActiveHour = if (room.challenge != null && room.challenge!!.isActiveHour) {
                this.room.challenge!!.toUiModel()
            } else {
                null
            }

            return RoomUiModel.Active(
                id = room.id,
                isSelected = isSelected,
                petName = room.pet.name,
                petAvatarId = PetEggConfig.getDrawableByType(room.pet.petEggType),
                userAvatarUrl = currentUser.avatarId,
                friendAvatarUrl = partnerUser?.avatarId,
                isStreakActive = room.isStreakActive,
                streakCount = room.streakCount,
                isActiveHour = room.challenge?.isActiveHour ?: false,
                challengeUiModel = challengeForActiveHour
            )
        }

        else -> RoomUiModel.Placeholder(id = room.id, isSelected = isSelected)
    }
}