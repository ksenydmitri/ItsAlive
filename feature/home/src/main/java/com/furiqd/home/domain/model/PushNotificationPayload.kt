package com.furiqd.home.domain.model

data class PushNotificationPayload(
    val roomId: String,
    val senderId: String,
    val recipientId: String
)