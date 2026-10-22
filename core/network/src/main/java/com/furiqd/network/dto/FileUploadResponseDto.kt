package com.furiqd.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponseDto(
    @SerialName("fileURL") val fileUrl: String
)