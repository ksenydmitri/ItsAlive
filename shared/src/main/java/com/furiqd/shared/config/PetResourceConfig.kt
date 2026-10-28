package com.furiqd.shared.config

import androidx.annotation.DrawableRes
import com.furiqd.shared.R
import com.furiqd.shared.domain.model.PetEggType
import com.furiqd.shared.domain.model.PetStage

object PetResourceConfig {

    @DrawableRes
    fun getPetDrawable(stage: PetStage, eggType: PetEggType): Int {
        if (stage == PetStage.EGG) {
            return PetEggConfig.getDrawableByType(eggType)
        }

        return when (eggType) {
            PetEggType.BLUE -> when (stage) {
                PetStage.BABY -> R.drawable.baby_blue
                PetStage.CHILD -> R.drawable.child_blue
                PetStage.TEEN -> R.drawable.teen_blue
                PetStage.ADULT -> R.drawable.adult_blue
                PetStage.EGG -> error("Handled above")
            }

            PetEggType.GREEN -> when (stage) {
                PetStage.BABY -> R.drawable.baby_green
                PetStage.CHILD -> R.drawable.child_green
                PetStage.TEEN -> R.drawable.teen_green
                PetStage.ADULT -> R.drawable.adult_green
                PetStage.EGG -> error("Handled above")
            }

            PetEggType.ORANGE -> when (stage) {
                PetStage.BABY -> R.drawable.baby_orange
                PetStage.CHILD -> R.drawable.child_orange
                PetStage.TEEN -> R.drawable.teen_orange
                PetStage.ADULT -> R.drawable.adult_orange
                PetStage.EGG -> error("Handled above")
            }

            PetEggType.PURPLE -> when (stage) {
                PetStage.BABY -> R.drawable.baby_purple
                PetStage.CHILD -> R.drawable.child_purple
                PetStage.TEEN -> R.drawable.teen_purple
                PetStage.ADULT -> R.drawable.adult_purple
                PetStage.EGG -> error("Handled above")
            }
        }
    }
}