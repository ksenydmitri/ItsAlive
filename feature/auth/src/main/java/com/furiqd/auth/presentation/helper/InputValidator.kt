package com.furiqd.auth.presentation.helper

import com.furiqd.auth.domain.model.PasswordStrength
import com.furiqd.auth.domain.model.ValidationError

object InputValidator {
    private val emailRegex = Regex("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")

    fun validateEmail(email: String): ValidationError.Email? {
        if (email.isBlank()) return ValidationError.Email.Empty
        if (!emailRegex.matches(email)) return ValidationError.Email.Invalid
        return null
    }

    fun validatePassword(password: String): ValidationError.Password? {
        if (password.isBlank()) return ValidationError.Password.Empty
        if (password.length < 8) return ValidationError.Password.TooShort

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        if (!hasUpperCase || !hasLowerCase || !hasDigit) {
            return ValidationError.Password.InvalidCharacters
        }
        return null
    }

    fun passwordStrength(password: String): PasswordStrength =
        PasswordStrength.calculate(password)
}