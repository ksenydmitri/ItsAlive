package com.furiqd.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RoomWithChallengeEntity(
    @Embedded
    val room: RoomEntity,

    @Relation(
        parentColumn = "challengeId",
        entityColumn = "id"
    )
    val challenge: DailyChallengeEntity?
)