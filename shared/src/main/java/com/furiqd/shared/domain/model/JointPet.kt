package com.furiqd.shared.domain.model

import java.util.Date

data class JointPet(
    val id: String = "",
    val name: String = "Egg",
    val level: Int = 1,
    val experience: Int = 0,
    val stage: PetStage = PetStage.EGG,
    val hatchedAt: Date? = null,
    val petEggType: PetEggType = PetEggType.GREEN,
    val petMood: PetMood = PetMood.HAPPY,
)

enum class PetStage {
    EGG, BABY, CHILD, TEEN, ADULT
}

enum class PetMood { HAPPY, SLEEPING, LONELY, EXCITED }