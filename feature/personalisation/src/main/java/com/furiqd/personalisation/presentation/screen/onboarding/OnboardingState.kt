package com.furiqd.personalisation.presentation.screen.onboarding

import androidx.compose.runtime.Immutable
import com.furiqd.shared.domain.model.Avatar


@Immutable
data class OnboardingState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val name: String = "",
    val localAvatarUri: String? = null,
    val selectedCustomBytes: ByteArray? = null,
    val selectedPresetColorHex: String = "#FFD2C2",
    val avatars: List<Avatar> = emptyList(),
    val isNicknameError: Boolean = false,
    val isNicknameTaken: Boolean = false,
    val isNameError: Boolean = false,
    val isNextButtonEnabled: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OnboardingState

        if (isLoading != other.isLoading) return false
        if (isNicknameError != other.isNicknameError) return false
        if (isNicknameTaken != other.isNicknameTaken) return false
        if (isNameError != other.isNameError) return false
        if (isNextButtonEnabled != other.isNextButtonEnabled) return false
        if (nickname != other.nickname) return false
        if (name != other.name) return false
        if (localAvatarUri != other.localAvatarUri) return false
        if (selectedPresetColorHex != other.selectedPresetColorHex) return false
        if (!selectedCustomBytes.contentEquals(other.selectedCustomBytes)) return false
        if (avatars != other.avatars) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + isNicknameError.hashCode()
        result = 31 * result + isNicknameTaken.hashCode()
        result = 31 * result + isNameError.hashCode()
        result = 31 * result + isNextButtonEnabled.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (localAvatarUri?.hashCode() ?: 0)
        result = 31 * result + selectedPresetColorHex.hashCode()
        result = 31 * result + (selectedCustomBytes?.contentHashCode() ?: 0)
        result = 31 * result + avatars.hashCode()
        return result
    }
}