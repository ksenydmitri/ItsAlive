package com.furiqd.auth.presentation.screens.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.auth.R
import com.furiqd.auth.presentation.elements.AuthSecondaryButton
import com.furiqd.auth.presentation.elements.LoginWithGoogleButton

@Composable
fun StartActions(
    onGoogleClick: () -> Unit,
    onEmailSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginWithGoogleButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(10.dp))
        AuthSecondaryButton(
            text = "✉  Sign In with Email",
            onClick = onEmailSignInClick,
            enabled = !isLoading
        )
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New here? ",
                color = Color(0xFF76727D),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(
                onClick = onSignUpClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Create Account",
                    color = Color(0xFFE8555C),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
