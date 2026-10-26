package com.furiqd.network.dto

import android.os.Build
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    @SerialName("deviceToken")
    val deviceToken: String,

    @SerialName("deviceId")
    val deviceId: String,

    @SerialName("os")
    val os: String,

    @SerialName("osVersion")
    val osVersion: String,

    @SerialName("channels")
    val channels: List<String>,

    @SerialName("userId")
    val userId: String
)
@Serializable
data class DeviceRegistrationResponse(
    @SerialName("registrationId") val registrationId: String? = null,
    @SerialName("deviceToken") val deviceToken: String? = null,
    @SerialName("objectId") val objectId: String? = null
)