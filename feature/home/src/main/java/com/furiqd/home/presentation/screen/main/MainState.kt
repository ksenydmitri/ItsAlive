package com.furiqd.home.presentation.screen.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainState(
    val isLoading: Boolean = false,
    val rooms: List<RoomUiModel> = emptyList(),
    val userId: String = ""
) {
    val selectedRoom: RoomUiModel?
        get() = rooms.find { it.isSelected }

    private val selectedIndex: Int
        get() = rooms.indexOf(selectedRoom)

    val previousRoom: RoomUiModel?
        get() = if (selectedIndex > 0) rooms[selectedIndex - 1] else null

    val nextRoom: RoomUiModel?
        get() = if (selectedIndex in 0 until rooms.lastIndex)
            rooms[selectedIndex + 1] else null
}