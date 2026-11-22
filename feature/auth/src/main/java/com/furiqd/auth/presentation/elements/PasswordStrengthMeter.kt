package com.furiqd.auth.presentation.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.auth.domain.model.PasswordStrength

@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (strength == PasswordStrength.EMPTY) return

    val filledSegments = when (strength) {
        PasswordStrength.EMPTY -> 0
        PasswordStrength.WEAK -> 1
        PasswordStrength.MEDIUM -> 2
        PasswordStrength.STRONG -> 4
    }

    val labelText = when (strength) {
        PasswordStrength.EMPTY -> ""
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

    val mintColor = Color(0xFF4ECDC4)
    val unfilledColor = Color(0xFFE4E2E5)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < filledSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isFilled) mintColor else unfilledColor)
                )
            }
        }

        Text(
            text = labelText,
            color = Color(0xFF258A83),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
