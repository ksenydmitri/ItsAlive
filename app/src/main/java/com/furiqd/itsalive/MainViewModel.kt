package com.furiqd.itsalive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import com.furiqd.shared.domain.usecase.SyncFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase
) : ViewModel() {
    val sessionState: StateFlow<UserSessionState> = userRepository.sessionState

    fun syncFcmToken() {
        viewModelScope.launch {
            try {
                val token = kotlinx.coroutines.withTimeout(5000L) {
                    FirebaseMessaging.getInstance().token.await()
                }
                syncFcmTokenUseCase(token)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to sync token", e)
            }
        }
    }
}