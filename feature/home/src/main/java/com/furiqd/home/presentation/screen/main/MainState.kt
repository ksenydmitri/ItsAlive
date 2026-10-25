package com.furiqd.home.presentation.screen.main

import androidx.compose.runtime.Immutable
import com.furiqd.home.presentation.model.RoomUiModel

@Immutable
data class MainState(
    val isLoading: Boolean = false,
    val rooms: List<RoomUiModel> = emptyList(),
    val userId: String = ""
) {
    val selectedRoom: RoomUiModel?
        get() = rooms.find { it.isSelected }
}