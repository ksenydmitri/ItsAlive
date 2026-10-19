package com.furiqd.pairing.presentation.screen.hostroom

import com.furiqd.shared.domain.model.PairingRoom

sealed interface HostRoomState {
    data object Loading : HostRoomState

    data class Success(
        val room: PairingRoom,
        val timeLeft: Long?,
        val isExpired: Boolean,
        val isCopied: Boolean
    ) : HostRoomState

    data class Error(val message: String?) : HostRoomState
}
