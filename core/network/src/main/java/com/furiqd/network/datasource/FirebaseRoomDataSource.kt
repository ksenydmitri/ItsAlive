package com.furiqd.network.datasource

import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.dto.PairingRoomDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseRoomDataSource @Inject constructor(
    val firestore: FirebaseFirestore
) : RemoteRoomDataSource {

    private val roomsCollection = firestore.collection("rooms")

    override fun observeRoom(roomId: String): Flow<PairingRoomDto?> {
        return roomsCollection.document(roomId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject<PairingRoomDto>()
                } else {
                    null
                }
            }
    }

    override suspend fun sendNudge(roomId: String, senderUserId: String) {
        roomsCollection.document(roomId)
            .update(
                "lastNudgeBy", senderUserId,
                "lastNudgeTimestamp", FieldValue.serverTimestamp()
            )
            .await()
    }

    override suspend fun getRoomDetails(roomId: String): PairingRoomDto? {
        val snapshot = roomsCollection.document(roomId).get().await()
        return if (snapshot.exists()) snapshot.toObject<PairingRoomDto>() else null
    }

    override fun observeUserRooms(userId: String): Flow<List<PairingRoomDto>> {
        return roomsCollection
            .where(
                Filter.or(
                    Filter.equalTo("hostId", userId),
                    Filter.equalTo("friendId", userId)
                )
            )
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { it.toObject<PairingRoomDto>() }
            }
    }

    override suspend fun deleteRoom(roomId: String) {
        roomsCollection.document(roomId).delete().await()
    }

    override suspend fun updateRoomChallengeId(roomId: String, challengeId: String) {
        roomsCollection.document(roomId)
            .update("challengeId", challengeId)
            .await()
    }
}