package com.furiqd.auth.presentation.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, Color(0xFF4ECDC4)),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF258A83)
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF258A83),
            letterSpacing = 0.5.sp
        )
    }
}
