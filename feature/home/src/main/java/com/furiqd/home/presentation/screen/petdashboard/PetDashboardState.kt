package com.furiqd.home.presentation.screen.petdashboard

import com.furiqd.shared.domain.model.JointPet

data class PetDashboardState(
    val pet: JointPet? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)