package com.furiqd.home.data.repository

import com.furiqd.home.domain.repository.PetDashboardRepository
import com.furiqd.local.contract.PetLocalDataSource
import com.furiqd.shared.data.mapper.toDomain
import com.furiqd.shared.domain.model.JointPet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class PetDashboardRepositoryImpl@Inject constructor(
    private val petLocalDataSource: PetLocalDataSource
): PetDashboardRepository {
    override fun observePetState(): Flow<JointPet> {
        return petLocalDataSource.observeCachedPet().map { it?.toDomain()
            ?: JointPet()
        }
    }
}