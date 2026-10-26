package com.furiqd.network.model

object PushPayloadKeys {
    const val ROOM_ID = "roomId"
    const val SENDER_ID = "senderId"
    const val TYPE = "type"
    const val ANDROID_PRIORITY = "android-priority"
}

object PushPriority {
    const val LOW = "low"
    const val HIGH = "high"
}

enum class PushNotificationType {
    INCITE_FRIEND
}