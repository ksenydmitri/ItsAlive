package com.furiqd.network.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var avatarId: String? = null,
    @get:PropertyName("isOnboardingCompleted")
    @set:PropertyName("isOnboardingCompleted")
    @PropertyName("isOnboardingCompleted")
    var isOnboardingCompleted: Boolean = false,
    var email: String? = null,
    var nickName: String? = null,
    var roomIds: List<String> = emptyList(),
    var fcmToken: String? = null
)