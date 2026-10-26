package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.model.PushNotificationPayload
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.shared.domain.repository.RoomRepository
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class InciteFriendUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        roomId: String
    ): Result<Unit> = runCatching {
        val currentUser = userRepository.observeUser().firstOrNull()
            ?: throw IllegalStateException("Current user not found")
        val room = roomRepository.fetchRoomDetails(roomId).getOrThrow()

        val targetUserId = if (room.hostId == currentUser.id) {
            room.friendId
        } else {
            room.hostId
        }

        require(!targetUserId.isNullOrBlank()) {
            "Partner ID not found for room $roomId"
        }

        val payload = PushNotificationPayload(
            roomId = roomId,
            senderId = currentUser.id,
            recipientId = targetUserId
        )
        homeRepository.inciteFriend(payload)
    }
}