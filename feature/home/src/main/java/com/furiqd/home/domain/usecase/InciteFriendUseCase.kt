package com.furiqd.home.domain.usecase

import android.util.Log
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.home.presentation.screen.main.RoomUiModel
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.repository.RoomRepository
import javax.inject.Inject

internal class InciteFriendUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: String, currentUserId: String): Result<Unit> {
        val room = roomRepository.fetchRoomDetails(roomId).getOrThrow()
        Log.d("InciteFriendUseCase", "Room: $room")
        val recipientId = if (currentUserId == room.hostId) room.friendId else room.hostId

        Log.d("InciteFriendUseCase", "Recipient ID: $recipientId")
        if (recipientId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Invalid room"))
        }

        Log.d("InciteFriendUseCase", "Recipient ID: $recipientId")
        return homeRepository.inciteFriend(
            roomId = room.id,
            senderId = currentUserId,
            recipientId = recipientId
        )
    }
}