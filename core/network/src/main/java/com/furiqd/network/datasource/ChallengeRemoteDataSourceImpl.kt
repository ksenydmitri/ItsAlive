package com.furiqd.network.datasource

import android.util.Log
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.dto.BackendlessPushRequest
import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.network.dto.DeliveryOptions
import com.furiqd.network.dto.PushPolicy
import com.furiqd.network.model.PushNotificationType
import com.furiqd.network.model.PushPayloadKeys
import com.furiqd.network.model.PushPriority
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ChallengeRemoteDataSourceImpl @Inject constructor(
    private val backendlessApi: BackendlessApi
) : RemoteChallengeDataSource {

    override suspend fun getChallengeById(challengeId: String): DailyChallengeDto? {
        return runCatching {
            backendlessApi.getChallengeByIdWithTask(challengeId)
        }.getOrNull()
    }

    override suspend fun uploadUserPhoto(
        challengeId: String,
        userId: String,
        photoBytes: ByteArray,
        mimeType: String
    ): String {
        val fileName = "challenge_${challengeId}_${userId}.jpg"
        val requestBody = photoBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", fileName, requestBody)

        return backendlessApi.uploadChallengeFile(fileName, multipartBody).fileUrl
    }

    override suspend fun updatePhotoUrl(
        challengeId: String,
        isHost: Boolean,
        photoPath: String
    ) {
        val fieldName = if (isHost) "userPhotoUrl" else "friendPhotoUrl"
        val body = mapOf(fieldName to photoPath)

        Log.e("ChallengeRemoteDataSource", "updatePhotoUrl: $body")
        backendlessApi.updateChallengePhoto(challengeId, body)
    }

    override suspend fun createChallenge(challengeDto: DailyChallengeDto): DailyChallengeDto {
        return backendlessApi.createChallenge(challengeDto)
    }

    override suspend fun getRandomTask(): ChallengeTaskDto? {
        return runCatching {
            val tasks = backendlessApi.getChallengeTasksList()
            tasks.randomOrNull()
        }.getOrNull()
    }

    override suspend fun inciteFriend(
        roomId: String,
        senderId: String,
        recipientId: String
    ) {
        val sanitizedRecipientId = recipientId.replace("'", "''")

        val payloadData = mapOf(
            PushPayloadKeys.TYPE to PushNotificationType.INCITE_FRIEND.name,
            PushPayloadKeys.ROOM_ID to roomId,
            PushPayloadKeys.SENDER_ID to senderId,
            PushPayloadKeys.ANDROID_PRIORITY to PushPriority.HIGH
        )

        backendlessApi.sendPushNotification(
            channelName = DEFAULT_CHANNEL,
            request = BackendlessPushRequest(
                message = "DATA_PUSH",
                deliveryOptions = DeliveryOptions(
                    pushPolicy = PushPolicy.ONLY,
                    whereClause = "userId = '$sanitizedRecipientId'"
                ),
                headers = payloadData
            )
        )
    }

    private companion object {
        const val DEFAULT_CHANNEL = "default"
    }
}