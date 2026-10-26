package com.furiqd.shared.domain.repository

import java.io.InputStream

fun interface ImageStreamProvider {
    fun openStream(): InputStream?
}