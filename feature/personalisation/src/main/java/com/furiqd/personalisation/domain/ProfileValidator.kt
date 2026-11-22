package com.furiqd.personalisation.domain

import java.text.Normalizer
import java.util.Locale

enum class ProfileField {
    NAME,
    NICKNAME
}

enum class ProfileValidationReason {
    REQUIRED,
    TOO_SHORT,
    TOO_LONG,
    INVALID_FORMAT
}

data class ProfileValidationError(
    val field: ProfileField,
    val reason: ProfileValidationReason
)

class ProfileValidationException(
    val error: ProfileValidationError
) : IllegalArgumentException("Invalid ${error.field.name.lowercase(Locale.ROOT)}")

data class ValidatedProfile(
    val name: String,
    val nickName: String
)

object ProfileValidator {
    private val namePattern = Regex("\\p{L}+(?:[ '-]\\p{L}+)*")
    private val nickNamePattern = Regex("[a-z0-9_.]+")

    fun validate(
        name: String,
        nickName: String?,
        unchangedExistingNickName: String? = null
    ): Result<ValidatedProfile> {
        return validateName(name).fold(
            onSuccess = { normalizedName ->
                val nicknameResult = if (
                    nickName != null && nickName == unchangedExistingNickName
                ) {
                    Result.success(nickName)
                } else {
                    validateNickName(nickName)
                }
                nicknameResult.map { normalizedNickName ->
                    ValidatedProfile(normalizedName, normalizedNickName)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    fun validateName(name: String): Result<String> {
        val normalized = Normalizer.normalize(name, Normalizer.Form.NFC)
        if (normalized.anyCodePoint(Character::isISOControl)) {
            return invalid(ProfileField.NAME)
        }

        val collapsed = collapseWhitespace(normalized)
        if (collapsed.isEmpty()) return invalid(ProfileField.NAME, ProfileValidationReason.REQUIRED)

        val codePointCount = collapsed.codePointCount(0, collapsed.length)
        if (codePointCount < 2) {
            return invalid(ProfileField.NAME, ProfileValidationReason.TOO_SHORT)
        }
        if (codePointCount > 30) {
            return invalid(ProfileField.NAME, ProfileValidationReason.TOO_LONG)
        }
        if (!namePattern.matches(collapsed)) {
            return invalid(ProfileField.NAME)
        }
        return Result.success(collapsed)
    }

    fun validateNickName(nickName: String?): Result<String> {
        if (nickName.isNullOrEmpty()) {
            return invalid(ProfileField.NICKNAME, ProfileValidationReason.REQUIRED)
        }

        val normalized = nickName.lowercase(Locale.ROOT)
        if (normalized.length < 2) {
            return invalid(ProfileField.NICKNAME, ProfileValidationReason.TOO_SHORT)
        }
        if (normalized.length > 24) {
            return invalid(ProfileField.NICKNAME, ProfileValidationReason.TOO_LONG)
        }
        if (!nickNamePattern.matches(normalized)) {
            return invalid(ProfileField.NICKNAME)
        }
        return Result.success(normalized)
    }

    private fun collapseWhitespace(value: String): String {
        val result = StringBuilder()
        var pendingSpace = false
        var index = 0

        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            index += Character.charCount(codePoint)
            if (Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)) {
                pendingSpace = result.isNotEmpty()
            } else {
                if (pendingSpace) result.append(' ')
                result.appendCodePoint(codePoint)
                pendingSpace = false
            }
        }
        return result.toString()
    }

    private fun String.anyCodePoint(predicate: (Int) -> Boolean): Boolean {
        var index = 0
        while (index < length) {
            val codePoint = codePointAt(index)
            if (predicate(codePoint)) return true
            index += Character.charCount(codePoint)
        }
        return false
    }

    private fun invalid(
        field: ProfileField,
        reason: ProfileValidationReason = ProfileValidationReason.INVALID_FORMAT
    ): Result<Nothing> = Result.failure(ProfileValidationException(ProfileValidationError(field, reason)))
}
