package com.furiqd.shared.data.mapper

import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ImageUrlFormatter @Inject constructor(
    @Named("workerEndpoint") private val workerEndpoint: String
) {
    fun format(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path

        return workerEndpoint.toHttpUrl().newBuilder()
            .addQueryParameter("name", path)
            .build()
            .toString()
    }
}