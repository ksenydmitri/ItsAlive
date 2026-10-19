package com.furiqd.home.domain.model

import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.JointPet
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.User

data class HomeRoomDetails(
    val room: PairingRoom,
    val currentUser: User,
    val partnerUser: User?,
) {
    val isHost: Boolean get() = room.hostId == currentUser.id
}