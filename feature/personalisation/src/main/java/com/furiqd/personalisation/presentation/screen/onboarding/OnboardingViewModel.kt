package com.furiqd.personalisation.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.personalisation.domain.ProfileValidator
import com.furiqd.personalisation.domain.SaveUserUseCase
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.repository.UserRepository
import com.furiqd.shared.domain.usecase.UpdateAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserUseCase: SaveUserUseCase,
    private val userRepository: UserRepository,
    private val updateAvatarUseCase: UpdateAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<OnboardingEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadExistingProfile()
    }

    private fun loadExistingProfile() {
        viewModelScope.launch {
            val currentUser = userRepository.getUser()
            if (currentUser != null && (currentUser.name.isNotBlank() || !currentUser.nickName.isNullOrBlank())) {
                _uiState.update { state ->
                    val newName = if (state.name.isBlank()) currentUser.name else state.name
                    val newNickname = if (state.nickname.isBlank()) currentUser.nickName.orEmpty() else state.nickname
                    val updatedState = state.copy(
                        name = newName,
                        nickname = newNickname
                    )
                    validateAndCopy(updatedState)
                }
            }
        }
    }

    fun handleEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NicknameChanged -> {
                _uiState.update { currentState ->
                    val isError = event.nickname.isNotEmpty() &&
                        ProfileValidator.validateNickName(event.nickname).isFailure
                    validateAndCopy(
                        currentState.copy(
                            nickname = event.nickname,
                            isNicknameError = isError,
                            isNicknameTaken = false
                        )
                    )
                }
                viewModelScope.launch {
                    val trimmed = event.nickname.trim()
                    if (trimmed.length >= 2 && ProfileValidator.validateNickName(trimmed).isSuccess) {
                        val isAvailable = userRepository.isNickNameAvailable(trimmed.lowercase())
                        _uiState.update { currentState ->
                            if (currentState.nickname == event.nickname) {
                                val taken = !isAvailable
                                validateAndCopy(
                                    currentState.copy(
                                        isNicknameTaken = taken
                                    )
                                )
                            } else {
                                currentState
                            }
                        }
                    }
                }
            }

            is OnboardingEvent.NameChanged -> {
                _uiState.update { currentState ->
                    val isError = event.name.isNotEmpty() &&
                        ProfileValidator.validateName(event.name).isFailure
                    validateAndCopy(
                        currentState.copy(
                            name = event.name,
                            isNameError = isError
                        )
                    )
                }
            }

            is OnboardingEvent.PresetColorSelected -> {
                _uiState.update { currentState ->
                    validateAndCopy(
                        currentState.copy(
                            selectedPresetColorHex = event.colorHex,
                            localAvatarUri = null,
                            selectedCustomBytes = null
                        )
                    )
                }
            }

            is OnboardingEvent.UploadCustomAvatarClicked -> {
                viewModelScope.launch {
                    _effect.send(OnboardingEffect.UploadAvatar)
                }
            }

            is OnboardingEvent.CustomAvatarSelected -> {
                _uiState.update { currentState ->
                    validateAndCopy(
                        currentState.copy(
                            localAvatarUri = event.uri,
                            selectedCustomBytes = event.bytes
                        )
                    )
                }
            }
            OnboardingEvent.NextClicked -> {
                saveUserProfile()
            }
        }
    }
    private fun validateAndCopy(state: OnboardingState): OnboardingState {
        val isNicknameValid = ProfileValidator.validateNickName(state.nickname).isSuccess && !state.isNicknameTaken
        val isNameValid = ProfileValidator.validateName(state.name).isSuccess
        val hasAvatar = true

        return state.copy(
            isNextButtonEnabled = isNicknameValid && isNameValid && hasAvatar
        )
    }

    private fun saveUserProfile() {
        if (_uiState.value.isLoading || !_uiState.value.isNextButtonEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isNextButtonEnabled = false) }

            val currentState = _uiState.value

            val finalAvatarUrl: String? = currentState.selectedCustomBytes?.let { bytes ->
                val uploadResult = updateAvatarUseCase(bytes)
                if (uploadResult.isFailure) {
                    _uiState.update { validateAndCopy(it.copy(isLoading = false)) }
                    _effect.send(OnboardingEffect.ShowError("Не удалось загрузить аватарку"))
                    return@launch
                }
                uploadResult.getOrNull()
            }

            val currentUser = userRepository.getUser()
            val updatedUser = User(
                id = currentUser?.id.orEmpty(),
                email = currentUser?.email,
                nickName = currentState.nickname.trim(),
                roomIds = currentUser?.roomIds.orEmpty(),
                avatarId = finalAvatarUrl,
                name = currentState.name.trim(),
                isOnboardingCompleted = true
            )

            saveUserUseCase(updatedUser)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(OnboardingEffect.NavigateToNext)
                }
                .onFailure { exception ->
                    _uiState.update { validateAndCopy(it.copy(isLoading = false)) }
                    _effect.send(
                        OnboardingEffect.ShowError(
                            exception.localizedMessage ?: "Ошибка сохранения"
                        )
                    )
                }
        }
    }
}