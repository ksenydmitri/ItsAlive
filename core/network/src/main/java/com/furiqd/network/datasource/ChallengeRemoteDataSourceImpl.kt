package com.furiqd.network.datasource

import android.util.Log
import com.furiqd.network.BuildConfig
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.dto.BackendlessPushRequest
import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ChallengeRemoteDataSourceImpl @Inject constructor(
    private val backendlessApi: BackendlessChallengeApi,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : RemoteChallengeDataSource {

    override suspend fun getChallengeById(challengeId: String): DailyChallengeDto? {
        return runCatching {
            backendlessApi.getChallengeByIdWithTask(challengeId)
        }.getOrNull()
    }

    override fun observeChallenge(challengeId: String): Flow<DailyChallengeDto> {
        TODO("Not yet implemented")
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
        backendlessApi.sendPushNotification(
            request = BackendlessPushRequest(
            message = "Друг зовет в игру!",
            pushSinglecast = listOf(recipientId),
            headers = mapOf(
                "android-ticker-text" to "Друг зовет в игру!",
                "android-content-title" to "Эй! Твой друг ждет 🐾",
                "android-content-text" to "Пора сделать совместное фото!",
                "roomId" to roomId,
                "type" to "INCITE_FRIEND"
                )
            )
        )
    }
}