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
import java.util.Date
import javax.inject.Inject

internal class FirebaseRoomDataSource @Inject constructor(
    val firestore: FirebaseFirestore
) : RemoteRoomDataSource {

    private val roomsCollection = firestore.collection("rooms")
    private val usersCollection = firestore.collection("users")

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

    override suspend fun createRoom(newRoomDto: PairingRoomDto): String {
        val documentRef = roomsCollection.add(newRoomDto).await()
        val userRef = usersCollection.document(newRoomDto.hostId)
        userRef.update("roomIds", FieldValue.arrayUnion(documentRef.id)).await()
        return documentRef.id
    }

    override suspend fun joinRoom(inviteCode: String, friendUserId: String): String {
        val querySnapshot = roomsCollection
            .whereEqualTo("code", inviteCode)
            .whereEqualTo("status", "WAITING")
            .limit(1)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            throw NoSuchElementException("Error: room not found or already occupied")
        }

        val document = querySnapshot.documents.first()
        val roomRef = document.reference
        val userRef = usersCollection.document(friendUserId)

        return firestore.runTransaction<String> { transaction ->
            val freshSnapshot = transaction.get(roomRef)
            val currentStatus = freshSnapshot.getString("status")

            if (currentStatus != "WAITING") {
                throw IllegalStateException("Room is no longer waiting")
            }

            transaction.update(roomRef, "friendId", friendUserId)
            transaction.update(roomRef, "status", "ACTIVE")
            transaction.update(roomRef, "pet.stage", "BABY")
            transaction.update(roomRef, "pet.hatchedAt", Date())
            transaction.update(userRef, "roomIds", FieldValue.arrayUnion(document.id))
            document.id
        }.await()
    }

    override suspend fun isCodeExisting(code: String): Boolean {
        val querySnapshot = roomsCollection
            .whereEqualTo("code", code)
            .whereIn("status", listOf("WAITING", "ACTIVE"))
            .get()
            .await()

        return !querySnapshot.isEmpty
    }
}