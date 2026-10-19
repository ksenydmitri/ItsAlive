package com.furiqd.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeTaskDto(
    @SerialName("objectId") val objectId: String = "",
    @SerialName("userTaskPrompt") val userTaskPrompt: String = "",
    @SerialName("friendTaskPrompt") val friendTaskPrompt: String = ""
)