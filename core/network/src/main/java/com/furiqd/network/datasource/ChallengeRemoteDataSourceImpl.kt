package com.furiqd.network.datasource

import android.util.Log
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ChallengeRemoteDataSourceImpl @Inject constructor(
    private val backendlessApi: BackendlessChallengeApi
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
}