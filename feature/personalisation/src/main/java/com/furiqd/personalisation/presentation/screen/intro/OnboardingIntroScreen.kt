package com.furiqd.personalisation.presentation.screen.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.shared.ui.elements.TextActionButton
import kotlinx.coroutines.launch

data class TutorialSlide(
    val badge: String,
    val title: String,
    val description: String,
    val iconEmoji: String
)

@Composable
fun OnboardingIntroScreen(
    onSkip: () -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = listOf(
        TutorialSlide(
            badge = "TOGETHER, EVERY DAY",
            title = "1 Photo a Day 📸",
            description = "Snap a live photo during the active hour to grow your streak.",
            iconEmoji = "📸"
        ),
        TutorialSlide(
            badge = "RAISE YOUR COMPANION",
            title = "Raise Your Pet 🥚",
            description = "Watch your joint pet hatch and evolve as your connection grows.",
            iconEmoji = "🐣"
        ),
        TutorialSlide(
            badge = "LIFE-SYNC HABITS",
            title = "Life-Sync 👟",
            description = "Sync steps, sleep, and shared locations for bonus pet energy.",
            iconEmoji = "👟"
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${String.format("%02d", pagerState.currentPage + 1)}/03",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF222026)
            )
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pager Tutorial Cards
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val slide = slides[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFFFF0EF))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = slide.badge,
                            color = Color(0xFFFF6B6B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Illustration Box
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F9F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slide.iconEmoji, fontSize = 72.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.03).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF76727D),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { index ->
                val isSelected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isSelected) Color(0xFFFF6B6B) else Color(0xFFD8D5DA))
                        .then(if (isSelected) Modifier.padding(horizontal = 12.dp) else Modifier.size(7.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Get Started Button
        TextActionButton(
            text = "Get Started",
            enabled = true,
            onClick = {
                if (pagerState.currentPage < slides.size - 1) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onGetStarted()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
