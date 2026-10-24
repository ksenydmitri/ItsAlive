package com.furiqd.home.presentation.screen.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.home.R
import com.furiqd.home.domain.usecase.DeleteRoomUseCase
import com.furiqd.home.domain.usecase.FetchRoomsUseCase
import com.furiqd.home.domain.usecase.InciteFriendUseCase
import com.furiqd.home.domain.usecase.SubmitChallengePhotoUseCase
import com.furiqd.home.domain.usecase.SyncFcmTokenUseCase
import com.furiqd.home.presentation.mapper.toUiModel
import com.furiqd.shared.domain.usecase.GetUserUseCase
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
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val submitChallengePhotoUseCase: SubmitChallengePhotoUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val inciteFriendUseCase: InciteFriendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<MainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeUserDataAndRooms()
        syncFcmToken()
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
                deleteRoom(event.roomId)
            }

            is MainEvent.InciteFriendClicked -> {
                inciteFriend()
            }

            is MainEvent.RoomChangedToNextInPager -> {
                val targetRoomId = _uiState.value.nextRoom?.id ?: return
                changeSelectedRoom(targetRoomId)
            }

            is MainEvent.RoomChangedToPreviousInPager -> {
                val targetRoomId = _uiState.value.previousRoom?.id ?: return
                changeSelectedRoom(targetRoomId)
            }

            is MainEvent.ChallengePhotoCaptured -> {
                uploadChallengePhoto(event.uri)
            }
        }
    }

    private fun observeUserDataAndRooms() {
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
            .catch {
                sendEffect(MainEffect.ShowToast(R.string.error_get_rooms))
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
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

    private fun uploadChallengePhoto(uri: Uri) {
        viewModelScope.launch {
            val roomId = uiState.value.selectedRoom?.id ?: return@launch
            submitChallengePhotoUseCase(
                roomId = roomId,
                photoPath = uri.toString(),
                userId = uiState.value.userId
            )
        }
    }

    private fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            deleteRoomUseCase(roomId)
        }
    }

    private fun inciteFriend() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Incite friend clicked")
            inciteFriendUseCase(
                uiState.value.selectedRoom?.id ?:
            return@launch, uiState.value.userId)
                .onSuccess {
                    Log.d("MainViewModel", "Incite friend success")
                    sendEffect(MainEffect.ShowToast(R.string.friend_incited_success))
                }
                .onFailure {
                    Log.d("MainViewModel", "Incite friend error")
                    sendEffect(MainEffect.ShowToast(R.string.friend_incited_error))
                }
        }
    }

    private fun sendEffect(effect: MainEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            syncFcmTokenUseCase(uiState.value.userId)
        }
    }
}