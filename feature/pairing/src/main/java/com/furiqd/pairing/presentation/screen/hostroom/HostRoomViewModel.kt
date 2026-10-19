package com.furiqd.pairing.presentation.screen.hostroom

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.usecase.GetPairingRoomUseCase
import com.furiqd.pairing.presentation.util.ClipboardManagerWrapper
import com.furiqd.pairing.presentation.util.ShareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostRoomViewModel@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPairingRoomUseCase: GetPairingRoomUseCase,
    private val clipboardManager: ClipboardManagerWrapper,
    private val shareManager: ShareManager
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _uiState = MutableStateFlow<HostRoomState>(HostRoomState.Loading)
    val uiState: StateFlow<HostRoomState> = _uiState.asStateFlow()

    private val _effect = Channel<HostRoomEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        Log.d("HostRoomViewModel", "init")
        loadRoom()
    }

    fun onEvent(event: HostRoomEvent) {
        when (event) {
            HostRoomEvent.OnCopyCodeClicked -> handleCopyCode()
            HostRoomEvent.OnShareClicked -> handleShareCode()
            HostRoomEvent.OnBackClicked -> {
                viewModelScope.launch { _effect.send(HostRoomEffect.NavigateBack) }
            }
        }
    }

    private fun loadRoom() {
        viewModelScope.launch {
            _uiState.update { HostRoomState.Loading }

            getPairingRoomUseCase(roomId).fold(
                onSuccess = { room ->
                    _uiState.update {
                        HostRoomState.Success(
                            room = room,
                            timeLeft = room.getMillisLeft(System.currentTimeMillis()),
                            isExpired = room.isExpired(System.currentTimeMillis()),
                            isCopied = false
                        )
                    }
                    if (!room.isExpired(System.currentTimeMillis())) {
                        startTimer(room)
                    }
                },
                onFailure = { error ->
                    _uiState.update { HostRoomState.Error(error.localizedMessage) }
                }
            )
        }
    }
    private fun startTimer(room: PairingRoom) {
        viewModelScope.launch {
            flow {
                while (true) {
                    emit(Unit)
                    delay(1000L)
                }
            }.collect {
                _uiState.update { state ->
                    (state as? HostRoomState.Success)?.copy(
                        timeLeft = room.getMillisLeft(System.currentTimeMillis()),
                        isExpired = false
                    ) ?: state
                }
            }
        }
    }

    private fun handleCopyCode() {
        val state = _uiState.value as? HostRoomState.Success ?: return
        viewModelScope.launch {
            _uiState.update { state.copy(isCopied = true) }
            clipboardManager.copyToClipboard(state.room.code)
            _effect.send(HostRoomEffect.ShowCopiedToast)
        }
    }

    private fun handleShareCode() {
        val state = _uiState.value as? HostRoomState.Success ?: return
        shareManager.shareInvite(state.room.code, state.room.pet.name)
    }
}