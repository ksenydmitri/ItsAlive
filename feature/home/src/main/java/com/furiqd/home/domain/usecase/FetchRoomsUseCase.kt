package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.model.HomeRoomDetails
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class FetchRoomsUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<HomeRoomDetails>> {
        return userRepository.observeUser().flatMapLatest { currentUser ->
            if (currentUser == null) return@flatMapLatest flowOf(emptyList())
            homeRepository.observeUserRooms(currentUser.id).map { rooms ->
                coroutineScope {
                    rooms.map { room ->
                        async {
                            val partnerId = if (room.hostId == currentUser.id) room.friendId else room.hostId
                            val partnerUser = partnerId?.let { userRepository.getUserById(it) }

                            HomeRoomDetails(
                                room = room,
                                currentUser = currentUser,
                                partnerUser = partnerUser
                            )
                        }
                    }.awaitAll()
                }
            }

        }
    }
}