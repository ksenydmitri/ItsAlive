package com.furiqd.personalisation.presentation.element

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun AvatarPickerSection(
    localAvatarUri: String?,
    isAvatarChosen: Boolean,
    selectedPresetColor: Color = Color(0xFFFFD2C2),
    onPresetSelected: (Color) -> Unit = {},
    onClick: () -> Unit
) {
    val presetColors = listOf(
        Color(0xFFFFD2C2),
        Color(0xFFB9F0EC),
        Color(0xFFFFE99A),
        Color(0xFFC8C4F3)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Avatar Circle with Camera + Badge
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (localAvatarUri.isNullOrBlank()) selectedPresetColor else Color.Transparent)
                    .border(3.dp, Color.White, CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (!localAvatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = localAvatarUri,
                        contentDescription = "Selected Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "K",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222026)
                    )
                }
            }

            // Plus Camera Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B6B))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Preset Avatar Color Circles
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presetColors.forEach { color ->
                val isSelected = color == selectedPresetColor && localAvatarUri.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.5.dp else 0.dp,
                            color = if (isSelected) Color(0xFFFF6B6B) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onPresetSelected(color) }
                )
            }
        }
    }
}