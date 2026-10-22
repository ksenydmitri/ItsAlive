package com.furiqd.network.datasource

import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.network.dto.FileUploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendlessChallengeApi {
    @GET("data/daily_challenges/{objectId}")
    suspend fun getChallengeById(
        @Path("objectId") challengeId: String
    ): DailyChallengeDto

    @GET("data/daily_challenges/{objectId}")
    suspend fun getChallengeByIdWithTask(
        @Path("objectId") challengeId: String,
        @Query("loadRelations") loadRelations: String = "taskId"
    ): DailyChallengeDto

    @POST("data/daily_challenges")
    @Headers("Content-Type: application/json")
    suspend fun createChallenge(
        @Body challengeDto: DailyChallengeDto
    ): DailyChallengeDto

    @PUT("data/daily_challenges/{objectId}")
    @Headers("Content-Type: application/json")
    suspend fun updateChallengePhoto(
        @Path("objectId") challengeId: String,
        @Body updates: Map<String, String>
    ): DailyChallengeDto

    @GET("data/daily_challenges")
    suspend fun findChallengeByRoom(
        @Query("where") whereClause: String
    ): List<DailyChallengeDto>

    @Multipart
    @POST("files/challenges/{fileName}?overwrite=true")
    suspend fun uploadChallengeFile(
        @Path("fileName") fileName: String,
        @Part file: MultipartBody.Part
    ): FileUploadResponseDto

    @GET("data/challenge_tasks?pageSize=100")
    suspend fun getChallengeTasksList(): List<ChallengeTaskDto>
}