package com.furiqd.network.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.furiqd.network.contract.RemoteAuthDataSource
import com.furiqd.network.dto.DeviceRegistrationRequest
import com.furiqd.network.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
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
        val documentSnapshot = usersCollection.document(userId).get().await()
        return if (documentSnapshot.exists()) {
            documentSnapshot.toObject<UserDto>()
        } else {
            null
        }
    }

    override suspend fun saveUserProfile(userDto: UserDto) {
        usersCollection.document(userDto.id)
            .set(userDto, SetOptions.merge())
    }

    override suspend fun checkOnboardingStatus(userId: String): Boolean {
        val profile = getUserProfile(userId)
        return profile?.isOnboardingCompleted ?: false
    }

    override suspend fun saveOnboardingStatus(userId: String, isCompleted: Boolean): Boolean {
        return try {
            val data = mapOf("isOnboardingCompleted" to isCompleted)
            usersCollection.document(userId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("HardwareIds")
    override suspend fun updateFcmToken(userId: String, token: String) {
        runCatching {
            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown_device_id"

            backendlessApi.registerDevice(
                request = DeviceRegistrationRequest(
                    deviceToken = token,
                    deviceId = deviceId,
                    os = "ANDROID",
                    osVersion = Build.VERSION.RELEASE ?: "14",
                    channels = listOf("default"),
                    userId = userId
                )
            )
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}