package com.furiqd.home.presentation.screen.petdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.furiqd.home.domain.repository.PetDashboardRepository
import com.furiqd.home.presentation.navigation.PetDashboardDestination
import com.furiqd.shared.domain.model.JointPet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PetDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petDashboardRepository: PetDashboardRepository
) : ViewModel() {

    private val roomId: String = savedStateHandle.toRoute<PetDashboardDestination>().roomId
    val uiState: StateFlow<PetDashboardState> =
        petDashboardRepository.observePetStateByRoomId(roomId)
        .map { petState -> PetDashboardState(pet = petState, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PetDashboardState(isLoading = true)
        )

    fun handleEvent(event: PetDashboardEvent) {
        when (event) {
            is PetDashboardEvent.FeedPet -> {
                viewModelScope.launch {
                    petDashboardRepository.updatePetState(JointPet(
                        id = uiState.value.pet?.id.orEmpty(),
                        name = uiState.value.pet?.name.orEmpty(),
                        level = uiState.value.pet?.level ?: 0,
                        experience = uiState.value.pet?.experience ?: 0
                    ))
                }
            }
            is PetDashboardEvent.PlayWithPet -> {
                viewModelScope.launch {
                    // Implement playing action
                }
            }
            is PetDashboardEvent.Refresh -> {
                // Refresh data
            }
        }
    }
}