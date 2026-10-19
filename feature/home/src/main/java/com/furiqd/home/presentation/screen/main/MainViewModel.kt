package com.furiqd.home.presentation.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.home.domain.usecase.DeleteRoomUseCase
import com.furiqd.home.domain.usecase.FetchRoomsUseCase
import com.furiqd.home.presentation.mapper.toUiModel
import com.furiqd.shared.domain.usecase.GetUserUseCase
import com.furiqd.shared.data.mapper.ImageUrlFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val fetchRoomsUseCase: FetchRoomsUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<MainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeUserDataAndRooms()
    }

    private fun observeUserDataAndRooms() {
        Log.e("MainViewModel", "observeUserDataAndRooms")
        getUserUseCase()
            .flatMapLatest { user ->
                val userId = user?.id.orEmpty()
                _uiState.update { it.copy(userId = userId) }

                if (userId.isNotBlank()) {
                    fetchRoomsUseCase()
                } else {
                    flowOf(emptyList())
                }
            }
            .onEach { pairingRooms ->
                Log.d("MainViewModel", "Rooms fetched: $pairingRooms")
                _uiState.update { currentState ->
                    val mappedRooms = pairingRooms.map { room ->
                        room.toUiModel(isSelected = false)
                    }

                    val fullList = mappedRooms + RoomUiModel.Placeholder()
                    val updatedList = selectRoomById(fullList, currentState.selectedRoom?.id)

                    currentState.copy(
                        rooms = updatedList,
                        isLoading = false
                    )
                }
            }
            .catch { throwable ->
                Log.e("MainViewModel", "Error fetching data", throwable)
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun handleEvent(event: MainEvent) {
        when (event) {
            is MainEvent.CameraClicked, MainEvent.PetClicked -> {
                sendEffect(MainEffect.TriggerPetAnimation)
            }

            is MainEvent.ProfileClicked -> {
                sendEffect(MainEffect.NavigateToProfile)
            }

            is MainEvent.CopyInviteCodeClicked -> {
                sendEffect(MainEffect.CopyToClipboard(event.code))
            }

            is MainEvent.OpenCreateRoomDialogClicked -> {
                sendEffect(MainEffect.NavigateToPairing)
            }

            is MainEvent.DeleteRoomClicked -> {
                viewModelScope.launch {
                    deleteRoomUseCase.invoke(event.roomId)
                }
            }

            is MainEvent.InciteFriendClicked -> {
                // TODO: Вызвать UseCase для пинга друга
                sendEffect(MainEffect.ShowToast("Friend nudged!"))
            }

            is MainEvent.RoomChangedToNextInPager -> {
                val targetRoomId = _uiState.value.nextRoom?.id ?: return
                changeSelectedRoom(targetRoomId)
            }

            is MainEvent.RoomChangedToPreviousInPager -> {
                val targetRoomId = _uiState.value.previousRoom?.id ?: return
                changeSelectedRoom(targetRoomId)
            }
        }
    }

    private fun changeSelectedRoom(roomId: String) {
        _uiState.update { currentState ->
            currentState.copy(rooms = selectRoomById(currentState.rooms, roomId))
        }
    }

    private fun selectRoomById(list: List<RoomUiModel>, targetId: String?): List<RoomUiModel> {
        val selectedId = targetId ?: list.firstOrNull()?.id
        return list.map { room ->
            val isSelected = room.id == selectedId
            when (room) {
                is RoomUiModel.Active -> room.copy(isSelected = isSelected)
                is RoomUiModel.Placeholder -> room.copy(isSelected = isSelected)
                is RoomUiModel.WaitingForFriend -> room.copy(isSelected = isSelected)
            }
        }
    }

    private fun sendEffect(effect: MainEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}