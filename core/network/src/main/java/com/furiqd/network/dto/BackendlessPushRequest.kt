package com.furiqd.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackendlessPushRequest(
    @SerialName("message") val message: String,
    @SerialName("deliveryOptions") val deliveryOptions: DeliveryOptions,
    @SerialName("headers") val headers: Map<String, String>
)

@Serializable
data class DeliveryOptions(
    @SerialName("pushPolicy") val pushPolicy: PushPolicy,
    @SerialName("whereClause") val whereClause: String
)

@Serializable
enum class PushPolicy {
    @SerialName("ONLY")
    ONLY,
    @SerialName("ALL")
    ALL
}