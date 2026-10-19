package com.furiqd.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyChallengeDto(
    @SerialName("objectId")val id: String = "",
    @SerialName("isActiveHour")val isActiveHour: Boolean = false,
    @SerialName("expiresAt")val expiresAt: Long? = null,
    @SerialName("task")val task: ChallengeTaskDto? = null,
    @SerialName("userPhotoUrl")val userPhotoUrl: String? = null,
    @SerialName("friendPhotoUrl")val friendPhotoUrl: String? = null,
    @SerialName("compositeImageUrl")val compositeImageUrl: String? = null
)