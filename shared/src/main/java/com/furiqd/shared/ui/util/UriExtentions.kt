package com.furiqd.shared.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.createImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = getExternalFilesDir("Pictures")

    val file = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )

    return FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        file
    )
}