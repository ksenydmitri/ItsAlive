package com.furiqd.home.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.shared.domain.usecase.GetUserUseCase
import com.furiqd.home.domain.usecase.LogoutUseCase
import com.furiqd.shared.domain.usecase.UpdateAvatarUseCase
import com.furiqd.shared.data.mapper.ImageUrlFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel@Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val imageFormatter: ImageUrlFormatter
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getUserUseCase.invoke()
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, isError = true) }
                }
                .collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = UserProfile(
                        id = user?.id ?: "",
                        name = user?.name ?: "",
                        email = user?.email ?: "",
                        avatarUrl = imageFormatter.format(user?.avatarId),
                        amountOfRooms = user?.roomIds?.size ?: 0
                    ),
                    isLoading = false,
                    isError = false
                )
            }
        }
    }

    fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LogoutClicked -> {
                viewModelScope.launch {
                    logoutUseCase.invoke()
                    _uiEffect.send(ProfileEffect.NavigateToAuth) }
            }
            is ProfileEvent.BackClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileEffect.NavigateToMain)
                }
            }
            is ProfileEvent.EditProfileClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileEffect.NavigateToEditProfile)
                }
            }
            is ProfileEvent.AvatarClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileEffect.AddAvatar)
                }
            }
            is ProfileEvent.AvatarSelected -> {
                viewModelScope.launch {
                    updateAvatarUseCase.invoke(event.bytes)
                }
            }
        }
    }
}