package com.furiqd.personalisation.presentation.screen.onboarding

sealed interface OnboardingEvent {
    data class NicknameChanged(val nickname: String) : OnboardingEvent
    data class NameChanged(val name: String) : OnboardingEvent
    data class PresetColorSelected(val colorHex: String) : OnboardingEvent
    object NextClicked : OnboardingEvent
    object UploadCustomAvatarClicked : OnboardingEvent
    class CustomAvatarSelected(val bytes: ByteArray, val uri: String) : OnboardingEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CustomAvatarSelected

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}