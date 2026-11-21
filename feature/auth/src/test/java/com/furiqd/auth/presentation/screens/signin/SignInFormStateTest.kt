package com.furiqd.auth.presentation.screens.signin

import com.furiqd.auth.domain.model.ValidationError
import com.furiqd.auth.presentation.elements.canSubmit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInFormStateTest {
    @Test
    fun submitRequiresValidEmailAndPassword() {
        assertFalse(SignInState().canSubmit())
        assertFalse(
            SignInState(email = "person@example.com")
                .copy(password = "Password1")
                .copy(emailError = ValidationError.Email.Invalid)
                .canSubmit()
        )
        assertFalse(
            SignInState(email = "person@example.com", password = "Password1")
                .copy(passwordError = ValidationError.Password.TooShort)
                .canSubmit()
        )
        assertTrue(
            SignInState(email = "person@example.com", password = "Password1").canSubmit()
        )
    }
}
