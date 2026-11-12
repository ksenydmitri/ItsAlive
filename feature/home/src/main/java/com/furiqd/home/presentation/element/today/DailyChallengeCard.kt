package com.furiqd.home.presentation.element.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.home.R
import com.furiqd.home.presentation.model.ChallengeDisplayState
import com.furiqd.home.presentation.model.ChallengeUiModel
import com.furiqd.home.presentation.model.RoomUiModel
import com.furiqd.shared.ui.util.rememberCountdownTimer

@Composable
internal fun DailyChallengeCard(
    room: RoomUiModel.Active,
    onCameraClick: () -> Unit,
    onInciteFriendClick: () -> Unit,
    onPetClick: () -> Unit
) {
    val challenge = room.challengeUiModel
    val deadline = when (val state = challenge?.displayState) {
        is ChallengeDisplayState.UserTurn -> state.deadlineMillis
        is ChallengeDisplayState.WaitingFriend -> state.deadlineMillis
        else -> null
    }
    val remaining = if (deadline == null) null else rememberCountdownTimer(deadline)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (remaining != null) {
            CountdownCard(remaining)
        }
        ChallengeCard(
            challenge = challenge,
            streakCount = room.streakCount,
            onCameraClick = onCameraClick,
            onInciteFriendClick = onInciteFriendClick,
            onPetClick = onPetClick
        )
    }
}

@Composable
private fun CountdownCard(remaining: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = TodayColors.peach,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = Color(0xFF9B7468),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.today_countdown_remaining, remaining),
                    color = Color(0xFF7A3B30),
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.today_countdown_caption),
                    color = Color(0xFF865F55),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: ChallengeUiModel?,
    streakCount: Int,
    onCameraClick: () -> Unit,
    onInciteFriendClick: () -> Unit,
    onPetClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_challenge_label),
                    color = Color(0xFF82727A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(color = Color(0xFFF3F2F5), shape = CircleShape) {
                    Text(
                        text = if (streakCount > 0) {
                            stringResource(R.string.today_day_label, streakCount)
                        } else {
                            stringResource(R.string.today_label)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        color = Color(0xFF68616E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = challenge?.userTask?.takeIf(String::isNotBlank)
                    ?: "Your next challenge is on its way.",
                color = TodayColors.text,
                fontSize = 20.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoStatus(
                    title = stringResource(R.string.today_photo_self),
                    completed = challenge?.isUserDone == true,
                    modifier = Modifier.weight(1f)
                )
                PhotoStatus(
                    title = stringResource(R.string.today_photo_partner),
                    completed = challenge?.isFriendDone == true,
                    waiting = challenge?.isFriendDone != true,
                    modifier = Modifier.weight(1f)
                )
            }
            when (challenge?.displayState) {
                is ChallengeDisplayState.UserTurn -> {
                    ChallengeAction(
                        text = stringResource(R.string.today_action_take_photo),
                        icon = Icons.Default.CameraAlt,
                        onClick = onCameraClick
                    )
                }
                is ChallengeDisplayState.WaitingFriend -> {
                    ChallengeAction(
                        text = stringResource(R.string.action_incite_friend),
                        icon = Icons.Default.NotificationsActive,
                        onClick = onInciteFriendClick
                    )
                }
                ChallengeDisplayState.CompositeReady -> {
                    ChallengeAction(
                        text = stringResource(R.string.today_action_visit_pet),
                        icon = Icons.Default.Pets,
                        onClick = onPetClick
                    )
                }
                ChallengeDisplayState.InactiveWaitingHour, null -> Unit
            }
        }
    }
}

@Composable
private fun ChallengeAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = TodayColors.coral,
            contentColor = Color(0xFF38282C)
        )
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun PhotoStatus(
    title: String,
    completed: Boolean,
    modifier: Modifier = Modifier,
    waiting: Boolean = false
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (waiting) Color(0xFFECE4DE) else Color(0xFFF4EEE8)),
            contentAlignment = Alignment.Center
        ) {
            if (completed || waiting) {
                Image(
                    painter = painterResource(R.drawable.today_coffee_preview),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(110.dp)
                        .alpha(if (waiting) 0.72f else 1f)
                )
                if (waiting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0x40ECE4DE))
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFFB6A69D),
                    modifier = Modifier.size(30.dp)
                )
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                shape = CircleShape,
                color = if (completed) Color(0xFFE4F7EF) else Color.White
            ) {
                Text(
                    text = stringResource(
                        when {
                            completed -> R.string.today_photo_uploaded
                            waiting -> R.string.today_photo_waiting
                            else -> R.string.today_photo_your_turn
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (completed) Color(0xFF23664F) else Color(0xFF655866),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = title,
            color = Color(0xFF716A74),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
