package com.furiqd.auth.presentation.screens.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.auth.R

@Composable
fun StartHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mascot Egg Scene with Soft Mint Halo
        Box(
            modifier = Modifier
                .size(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFE7F9F7), CircleShape)
            )
            Image(
                painter = painterResource(com.furiqd.shared.R.drawable.green_egg),
                contentDescription = stringResource(R.string.auth_pet_egg_description),
                modifier = Modifier.size(170.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Eyebrow Tag
        Box(
            modifier = Modifier
                .background(Color(0xFFE7F9F7), RoundedCornerShape(99.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "MADE FOR TWO",
                color = Color(0xFF258A83),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Small moments.\nMore us. 🐾",
            color = Color(0xFF222026),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A shared world for you and your favorite person.",
            color = Color(0xFF76727D),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
