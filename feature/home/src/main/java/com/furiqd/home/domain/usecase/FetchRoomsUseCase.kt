package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.model.HomeRoomDetails
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

internal class FetchRoomsUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<HomeRoomDetails>> {
        return userRepository.observeUser().flatMapLatest { currentUser ->
            if (currentUser == null) return@flatMapLatest flowOf(emptyList())

            homeRepository.observeUserRooms(currentUser.id).flatMapLatest { rooms ->
                if (rooms.isEmpty()) {
                    return@flatMapLatest flowOf(emptyList())
                }

                val partnerIds = rooms.mapNotNull { room ->
                    if (room.hostId == currentUser.id) room.friendId else room.hostId
                }.distinct()
                val partnerFlows: List<Flow<Pair<String, User?>>> = partnerIds.map { partnerId ->
                    kotlinx.coroutines.flow.flow {
                        val user = userRepository.getUserById(partnerId)
                        emit(partnerId to user)
                    }
                }

                if (partnerFlows.isEmpty()) {
                    flowOf(rooms.map { room ->
                        HomeRoomDetails(room = room, currentUser = currentUser, partnerUser = null)
                    })
                } else {
                    combine(partnerFlows) { partnersArray ->
                        val partnersMap = partnersArray.toMap()
                        rooms.map { room ->
                            val partnerId = if (room.hostId == currentUser.id) room.friendId else room.hostId
                            HomeRoomDetails(
                                room = room,
                                currentUser = currentUser,
                                partnerUser = partnerId?.let { partnersMap[it] }
                            )
                        }
                    }
                }
            }
        }
    }
}