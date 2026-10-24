package com.furiqd.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackendlessPushRequest(
    val message: String,
    val pushSinglecast: List<String>,
    val headers: Map<String, String>
)