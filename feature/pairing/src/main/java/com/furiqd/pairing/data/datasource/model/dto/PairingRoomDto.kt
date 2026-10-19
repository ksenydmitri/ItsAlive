package com.furiqd.pairing.data.datasource.model.dto

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class PairingRoomDto(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val friendId: String? = null,
    val pet: JointPetDto = JointPetDto(),
    val createdAt: Long = Date().time,
    val status: String = "WAITING",
    val challengeId: String? = null,
    val isStreakActive: Boolean = false,
    val streakCount: Int = 0,
    val isActiveHour: Boolean = false
)