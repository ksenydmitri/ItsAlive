package com.furiqd.local.datasource

import android.content.ContentResolver
import androidx.core.net.toUri
import com.furiqd.local.contract.ImageLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageLocalDataSourceImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : ImageLocalDataSource {

    companion object {
        private const val MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024
    }

    override suspend fun prepareImageBytes(rawBytes: ByteArray): ByteArray = withContext(
        Dispatchers.IO) {
        require(rawBytes.size <= MAX_IMAGE_SIZE_BYTES) {
            "Image size exceeds the 5MB limit"
        }
        require(isValidImageHeader(rawBytes)) {
            "Unsupported image format"
        }
        rawBytes
    }

    private fun isValidImageHeader(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false

        val isJpeg = bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
        val isPng = bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()
                && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        val isWebp = bytes.size > 11 &&
                bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
                bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
                bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()

        return isJpeg || isPng || isWebp
    }

    override suspend fun readFileAsBytes(uriString: String): Pair<ByteArray, String>?
    = withContext(Dispatchers.IO) {
        runCatching {
            val uri = uriString.toUri()
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            bytes to mimeType
        }.getOrNull()
    }
}