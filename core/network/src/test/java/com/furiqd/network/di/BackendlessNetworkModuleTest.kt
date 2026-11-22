package com.furiqd.network.di

import com.furiqd.network.dto.BackendlessPushRequest
import com.furiqd.network.dto.DeliveryOptions
import com.furiqd.network.dto.PushPolicy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test

class BackendlessNetworkModuleTest {
    @Test
    fun clientUsesBoundedTimeoutsAndSingleLoggingInterceptor() {
        val client = RemoteDataSourceModule.createOkHttpClient()

        assertEquals(15_000, client.connectTimeoutMillis)
        assertEquals(15_000, client.readTimeoutMillis)
        assertEquals(15_000, client.writeTimeoutMillis)
        assertEquals(1, client.interceptors.filterIsInstance<HttpLoggingInterceptor>().size)
        assertEquals(
            HttpLoggingInterceptor.Level.NONE,
            client.interceptors.filterIsInstance<HttpLoggingInterceptor>().single().level
        )
    }

    @Test
    fun sensitivePushHeadersAreSerializedAsHeaderFields() {
        val request = BackendlessPushRequest(
            message = "A friend sent you a visit",
            deliveryOptions = DeliveryOptions(PushPolicy.ALL, ""),
            headers = mapOf("roomId" to "room-1", "type" to "INCITE_FRIEND")
        )

        val serialized = Json.encodeToString(request)
        val headers = Json.parseToJsonElement(serialized).jsonObject["headers"]!!.jsonObject

        assertEquals("room-1", headers["roomId"]!!.jsonPrimitive.content)
        assertEquals("INCITE_FRIEND", headers["type"]!!.jsonPrimitive.content)
    }
}
