package com.furiqd.auth.presentation.elements

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.furiqd.shared.ui.theme.ItsAliveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthTextFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun errorTextIsVisible() {
        composeRule.setContent {
            ItsAliveTheme {
                AuthTextField(
                    value = "invalid",
                    onValueChange = {},
                    label = "Email",
                    errorText = "Enter a valid email"
                )
            }
        }

        composeRule.onNodeWithText("Enter a valid email").assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityCanBeToggled() {
        composeRule.setContent {
            ItsAliveTheme {
                AuthTextField(
                    value = "Password1",
                    onValueChange = {},
                    label = "Password",
                    isPassword = true
                )
            }
        }

        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun disabledFieldCannotBeEdited() {
        composeRule.setContent {
            ItsAliveTheme {
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    enabled = false,
                    modifier = Modifier.testTag("email-field")
                )
            }
        }

        composeRule.onNodeWithTag("email-field").assertIsNotEnabled()
    }
}
