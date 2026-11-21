package com.furiqd.auth.presentation.elements

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.furiqd.auth.domain.model.PasswordStrength
import com.furiqd.shared.ui.theme.ItsAliveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordStrengthMeterTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun weakAndStrongValuesSelectTheirVisualStates() {
        composeRule.setContent {
            ItsAliveTheme {
                PasswordStrengthMeter(PasswordStrength.WEAK)
                PasswordStrengthMeter(PasswordStrength.STRONG)
            }
        }

        composeRule.onNodeWithTag("password-strength-WEAK").assertIsDisplayed()
        composeRule.onNodeWithTag("password-strength-STRONG").assertIsDisplayed()
    }
}
