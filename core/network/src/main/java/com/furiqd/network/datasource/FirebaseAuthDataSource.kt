package com.furiqd.network.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.dto.DeviceRegistrationRequest
import com.furiqd.network.dto.UserDto
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val backendlessApi: BackendlessApi,
    @ApplicationContext private val context: Context
) : RemoteAuthDataSource {

    private val usersCollection = firestore.collection("users")

    override val currentUserIdFlow: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun getUserProfile(userId: String): UserDto? {
        return withFirestoreErrors {
            val documentSnapshot = usersCollection.document(userId).get().await()
            decodeUserProfile(documentSnapshot.exists()) {
                documentSnapshot.toObject<UserDto>()
            }
        }
    }

    override suspend fun saveUserProfile(userDto: UserDto) {
        withFirestoreErrors {
            usersCollection.document(userDto.id)
                .set(userDto, SetOptions.merge())
                .await()
        }
    }

    override suspend fun checkOnboardingStatus(userId: String): Boolean {
        val profile = getUserProfile(userId)
        return profile?.isOnboardingCompleted ?: false
    }

    override suspend fun isNickNameAvailable(nickName: String): Boolean {
        return try {
            val snapshot = usersCollection.whereEqualTo("nickName", nickName).get().await()
            snapshot.isEmpty
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun saveOnboardingStatus(userId: String, isCompleted: Boolean): Boolean {
        return try {
            val data = mapOf("isOnboardingCompleted" to isCompleted)
            withFirestoreErrors {
                usersCollection.document(userId)
                    .set(data, SetOptions.merge())
                    .await()
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: RemoteFirestoreException) {
            false
        }
    }

    @SuppressLint("HardwareIds")
    override suspend fun updateFcmToken(userId: String, token: String) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device_id"

        backendlessApi.registerDevice(
            request = createDeviceRegistrationRequest(
                userId = userId,
                token = token,
                deviceId = deviceId,
                osVersion = Build.VERSION.RELEASE ?: "14"
            )
        )
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

internal class RemoteFirestoreException(cause: FirebaseFirestoreException) :
    Exception("Remote profile operation failed", cause)

internal suspend fun <T> withFirestoreErrors(block: suspend () -> T): T = try {
    block()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: FirebaseFirestoreException) {
    throw RemoteFirestoreException(failure)
}

internal fun decodeUserProfile(
    documentExists: Boolean,
    decode: () -> UserDto?
): UserDto? = if (documentExists) decode() else null

internal fun createDeviceRegistrationRequest(
    userId: String,
    token: String,
    deviceId: String,
    osVersion: String
): DeviceRegistrationRequest = DeviceRegistrationRequest(
    deviceToken = token,
    deviceId = deviceId,
    os = "ANDROID",
    osVersion = osVersion,
    channels = listOf("default"),
    userId = userId
)