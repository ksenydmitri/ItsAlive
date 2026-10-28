package com.furiqd.shared.data.mapper

import com.furiqd.shared.domain.model.JointPet
import com.furiqd.shared.domain.model.PetEggType
import com.furiqd.local.entity.JointPetEntity
import com.furiqd.local.entity.RoomPetEmbedded
import com.furiqd.network.dto.JointPetDto
import com.furiqd.shared.domain.model.PetStage
import java.util.Date

fun JointPetEntity.toDomain() = JointPet(
    id = id,
    name = name,
    level = level,
    experience = experience,
    stage = stage.toPetStage(),
    hatchedAt = hatchedAt?.let { Date(it) }
)

fun JointPet.toEntity() = JointPetEntity(
    id = id,
    name = name,
    level = level,
    experience = experience,
    stage = stage.toString(),
    hatchedAt = hatchedAt?.time,
    petEggType = petEggType.id
)

fun JointPetDto.toDomain(): JointPet {
    return JointPet(
        id = this.id,
        name = this.name,
        level = this.level,
        experience = this.experience,
        stage = this.stage.toPetStage(),
        hatchedAt = this.hatchedAt,
        petEggType = PetEggType.fromId(this.petEggType)
    )
}
fun JointPet.toDto(): JointPetDto {
    return JointPetDto(
        id = this.id,
        name = this.name,
        level = this.level,
        experience = this.experience,
        stage = this.stage.toString(),
        hatchedAt = this.hatchedAt,
        petEggType = this.petEggType.id
    )
}

fun String?.toPetStage(): PetStage = when (this?.uppercase()) {
    "BABY" -> PetStage.BABY
    "CHILD" -> PetStage.CHILD
    "TEEN" -> PetStage.TEEN
    "ADULT" -> PetStage.ADULT
    else -> PetStage.EGG
}

fun RoomPetEmbedded.toDomain(): JointPet = JointPet(
    id = id,
    name = name,
    level = level,
    experience = experience,
    stage = stage.toPetStage(),
    hatchedAt = hatchedAt?.let { Date(it) },
    petEggType = PetEggType.fromId(petEggType)
)

fun JointPetDto.toEmbedded(): RoomPetEmbedded = RoomPetEmbedded(
    id = id,
    name = name,
    level = level,
    experience = experience,
    stage = stage,
    hatchedAt = hatchedAt?.time,
    petEggType = petEggType
)