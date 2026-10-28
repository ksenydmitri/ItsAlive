package com.furiqd.shared.config

import androidx.annotation.DrawableRes
import com.furiqd.shared.R
import com.furiqd.shared.domain.model.PetEggType
import com.furiqd.shared.domain.model.PetStage

object PetResourceConfig {

    @DrawableRes
    fun getPetDrawable(stage: PetStage, eggType: PetEggType): Int {
        return when (stage) {
            PetStage.EGG -> PetEggConfig.getDrawableByType(eggType)
            PetStage.BABY -> R.drawable.baby_blue
            PetStage.CHILD -> R.drawable.child_blue
            PetStage.TEEN -> R.drawable.teen_blue
            PetStage.ADULT -> R.drawable.adult_blue
        }
    }
}