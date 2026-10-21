package com.furiqd.network.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class PairingRoomDto(
    @DocumentId
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val friendId: String? = null,
    val pet: JointPetDto = JointPetDto(),
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "WAITING",
    val challengeId: String? = null,
    @get:PropertyName("streakActive")
    val isStreakActive: Boolean = false,
    val streakCount: Int = 0
)