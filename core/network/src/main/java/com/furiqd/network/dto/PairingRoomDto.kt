package com.furiqd.network.dto

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class PairingRoomDto(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val friendId: String? = null,
    val pet: JointPetDto = JointPetDto(),
    val createdAt: Date = Date(),
    val status: String = "WAITING",
    val challengeId: String? = null,
    val isStreakActive: Boolean = false,
    val streakCount: Int = 0
)