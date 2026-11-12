package com.furiqd.shared.domain.model

import java.util.Date

data class JointPet(
    val id: String = "",
    val name: String = "Egg",
    val level: Int = 1,
    val experience: Int = 0,
    val stage: PetStage = PetStage.fromStreak(0),
    val hatchedAt: Date? = null,
    val petEggType: PetEggType = PetEggType.GREEN,
    val petMood: PetMood = PetMood.HAPPY
)

enum class PetStage {
    EGG, BABY, CHILD, TEEN, ADULT;
    companion object {
        fun fromStreak(streakCount: Int): PetStage =
            when {
                streakCount >= 30 -> ADULT
                streakCount >= 14 -> TEEN
                streakCount >= 7 -> CHILD
                streakCount >= 3 -> BABY
                else -> EGG
            }
    }
}

enum class PetMood { HAPPY, SLEEPING, LONELY, EXCITED }