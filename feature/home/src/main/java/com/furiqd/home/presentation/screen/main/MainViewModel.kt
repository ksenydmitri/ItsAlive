package com.furiqd.home.presentation.screen.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.home.R
import com.furiqd.home.domain.model.HomeRoomDetails
import com.furiqd.home.domain.usecase.DeleteRoomUseCase
import com.furiqd.home.domain.usecase.FetchRoomsUseCase
import com.furiqd.home.domain.usecase.InciteFriendUseCase
import com.furiqd.home.domain.usecase.SubmitChallengePhotoUseCase
import com.furiqd.home.presentation.mapper.toUiModel
import com.furiqd.shared.domain.repository.UserRepository
import com.furiqd.shared.domain.usecase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val fetchRoomsUseCase: FetchRoomsUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val submitChallengePhotoUseCase: SubmitChallengePhotoUseCase,
    private val inciteFriendUseCase: InciteFriendUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    private var currentRoomDetails: List<HomeRoomDetails> = emptyList()

    private val _effect = Channel<MainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeUserDataAndRooms()
    }

    fun handleEvent(event: MainEvent) {
        when (event) {
            is MainEvent.UpdateFcmToken -> updateFcmToken(event.token)
            is MainEvent.CameraClicked, MainEvent.PetClicked -> sendEffect(MainEffect.TriggerPetAnimation)
            is MainEvent.ProfileClicked -> sendEffect(MainEffect.NavigateToProfile)
            is MainEvent.CopyInviteCodeClicked -> sendEffect(MainEffect.CopyToClipboard(event.code))
            is MainEvent.OpenCreateRoomDialogClicked -> sendEffect(MainEffect.NavigateToPairing)
            is MainEvent.DeleteRoomClicked -> deleteRoom(event.roomId)
            is MainEvent.InciteFriendClicked -> inciteFriend()
            is MainEvent.PageSelected -> {
                val targetRoom = _uiState.value.rooms.getOrNull(event.pageIndex) ?: return
                changeSelectedRoom(targetRoom.id)
            }
            is MainEvent.ChallengePhotoCaptured -> uploadChallengePhoto(event.uri)
        }
    }

    private fun observeUserDataAndRooms() {
        getUserUseCase()
            .map { it?.id.orEmpty() }
            .distinctUntilChanged()
            .onEach { userId ->
                _uiState.update { it.copy(userId = userId) }
            }
            .launchIn(viewModelScope)

        fetchRoomsUseCase()
            .onEach { pairingRooms ->
                currentRoomDetails = pairingRooms

                _uiState.update { currentState ->
                    val mappedRooms = pairingRooms.map { it.toUiModel() }
                    val fullList = mappedRooms + RoomUiModel.Placeholder()

                    val targetSelectedId = currentState.selectedRoom?.id
                        ?: mappedRooms.firstOrNull()?.id

                    val updatedList = selectRoomById(fullList, targetSelectedId)

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

            _uiState.update { it.copy(isLoading = true) }

            submitChallengePhotoUseCase(
                roomId = roomId,
                photoPath = uri.toString(),
                userId = uiState.value.userId
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                Log.e("MainViewModel", "Failed to upload photo", error)
                sendEffect(MainEffect.ShowToast(R.string.error_get_rooms))
            }
        }
    }

    private fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            runCatching {
                deleteRoomUseCase(roomId)
            }.onFailure { error ->
                Log.e("MainViewModel", "Failed to delete room", error)
                sendEffect(MainEffect.ShowToast(R.string.error_get_rooms))
            }
        }
    }

    private fun inciteFriend() {
        viewModelScope.launch {
            val selectedRoomId = uiState.value.selectedRoom?.id ?: return@launch

            inciteFriendUseCase(selectedRoomId)
                .onSuccess {
                    sendEffect(MainEffect.ShowToast(R.string.friend_incited_success))
                }
                .onFailure { error ->
                    Log.e("MainViewModel", "Incite friend error", error)
                    sendEffect(MainEffect.ShowToast(R.string.friend_incited_error))
                }
        }
    }

    private fun sendEffect(effect: MainEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun updateFcmToken(token: String) {
        viewModelScope.launch {
            userRepository.updateFcmToken(token)
        }
    }
}