package com.furiqd.network.datasource

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
            backendlessApi.getChallengeById(challengeId)
        }.getOrNull()
    }

    override fun observeChallenge(challengeId: String): Flow<DailyChallengeDto> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadUserPhoto(
        challengeId: String,
        userId: String,
        photoBytes: ByteArray
    ): String {
        val fileName = "challenge_${challengeId}_${userId}.jpg"
        val requestFile = photoBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", fileName, requestFile)

        val response = backendlessApi.uploadChallengeFile(fileName, multipartBody)
        return response
    }

    override suspend fun updatePhotoUrl(
        challengeId: String,
        isHost: Boolean,
        photoPath: String
    ) {
        val fieldName = if (isHost) "userPhotoUrl" else "friendPhotoUrl"
        val body = mapOf(fieldName to photoPath)

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