package com.furiqd.auth.domain.model

enum class PasswordStrength {
    EMPTY,
    WEAK,
    MEDIUM,
    STRONG;

    companion object {
        fun calculate(password: String): PasswordStrength {
            if (password.isEmpty()) return EMPTY
            if (password.length < 8) return WEAK

            val score = listOf(
                password.any(Char::isDigit),
                password.any(Char::isUpperCase),
                password.any { !it.isLetterOrDigit() },
            ).count { it } + 1

            return when {
                score == 4 -> STRONG
                score >= 2 -> MEDIUM
                else -> WEAK
            }
        }
    }
}
