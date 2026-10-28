package com.furiqd.home.domain.repository

import com.furiqd.shared.domain.model.JointPet
import kotlinx.coroutines.flow.Flow

internal interface PetDashboardRepository {
    fun observePetState(): Flow<JointPet>
}