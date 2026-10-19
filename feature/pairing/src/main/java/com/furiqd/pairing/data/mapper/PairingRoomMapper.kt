package com.furiqd.pairing.data.mapper

import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.RoomStatus
import com.furiqd.pairing.data.datasource.model.dto.PairingRoomDto

fun PairingRoomDto.toDomain(): PairingRoom {
    val domainStatus = when (this.status.uppercase()) {
        "WAITING" -> RoomStatus.WaitingForFriend
        "ACTIVE" -> RoomStatus.Active
        else -> RoomStatus.NotFound
    }

    return PairingRoom(
        id = this.id,
        code = this.code,
        hostId = this.hostId,
        friendId = this.friendId,
        pet = this.pet.toDomain(),
        createdAt = this.createdAt,
        status = domainStatus,
        isStreakActive = this.isStreakActive,
        streakCount = this.streakCount
    )
}