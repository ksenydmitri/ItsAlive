package com.furiqd.auth.domain.model

import com.furiqd.auth.presentation.helper.InputValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthCalculatorTest {
    @Test
    fun emptyPasswordHasEmptyStrength() {
        assertEquals(PasswordStrength.EMPTY, InputValidator.passwordStrength(""))
    }

    @Test
    fun shortPasswordIsWeak() {
        assertEquals(PasswordStrength.WEAK, InputValidator.passwordStrength("A1!"))
    }

    @Test
    fun lengthAndOneComplexitySignalAreWeak() {
        assertEquals(PasswordStrength.WEAK, InputValidator.passwordStrength("password"))
    }

    @Test
    fun lengthAndTwoComplexitySignalsAreMedium() {
        assertEquals(PasswordStrength.MEDIUM, InputValidator.passwordStrength("Password"))
    }

    @Test
    fun lengthAndAllComplexitySignalsAreStrong() {
        assertEquals(PasswordStrength.STRONG, InputValidator.passwordStrength("Password1!"))
    }
}
