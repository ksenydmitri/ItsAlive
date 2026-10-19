package com.furiqd.home.presentation.element

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.furiqd.home.R
import com.furiqd.home.presentation.screen.main.RoomUiModel
import com.furiqd.shared.ui.util.toFormattedTimer

private sealed interface ChallengeDisplayState {
    data object CompositeReady : ChallengeDisplayState
    data class ActiveUserTurn(val timeLeft: String) : ChallengeDisplayState
    data object ActiveWaitingFriend : ChallengeDisplayState
    data object InactiveWaitingHour : ChallengeDisplayState
}

@Composable
fun ActiveRoomCard(
    room: RoomUiModel.Active,
    onCameraClick: () -> Unit,
    onInciteFriendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayState = remember(room) {
        val challenge = room.challengeUiModel
        when {
            room.isActiveHour && challenge != null -> {
                if (!challenge.isUserDone) {
                    ChallengeDisplayState.ActiveUserTurn(
                        challenge.timeLeft?.toFormattedTimer() ?: "")
                } else {
                    ChallengeDisplayState.ActiveWaitingFriend
                }
            }
            else -> ChallengeDisplayState.InactiveWaitingHour
        }
    }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            StreakHeader(room = room)

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (displayState) {
                        is ChallengeDisplayState.CompositeReady -> {
                            Text(text = stringResource(R.string.game_composite_ready))
                        }

                        is ChallengeDisplayState.ActiveUserTurn -> {
                            ActiveChallengeContent(
                                timeLeft = displayState.timeLeft,
                                onCameraClick = onCameraClick
                            )
                        }

                        is ChallengeDisplayState.ActiveWaitingFriend -> {
                            WaitingFriendContent(
                                onInciteFriendClick = onInciteFriendClick
                            )
                        }

                        is ChallengeDisplayState.InactiveWaitingHour -> {
                            InactiveHourContent(petAvatarId = room.petAvatarId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakHeader(room: RoomUiModel.Active) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (room.isStreakActive) Color(0xFFFFEAE2)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        AvatarWithBorder(
            avatarUrl = room.userAvatarUrl,
            isDone = room.challengeUiModel?.isUserDone ?: false,
            modifier = Modifier.size(40.dp)
        )
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = stringResource(R.string.streak_description),
            tint = if (room.isStreakActive) Color(0xFFFF5722) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.streak_counter_active, room.streakCount),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = if (room.isStreakActive) Color(0xFFFF5722) else Color.Gray
        )
        AvatarWithBorder(
            avatarUrl = room.friendAvatarUrl,
            isDone = room.challengeUiModel?.isFriendDone ?: false,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun ActiveChallengeContent(
    timeLeft: String,
    onCameraClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.game_active_alert),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$timeLeft ${stringResource(R.string.game_time_left_suffix)}",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.action_capture_element),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun WaitingFriendContent(
    onInciteFriendClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.action_waiting_friend),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onInciteFriendClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.action_incite_friend),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun InactiveHourContent(petAvatarId: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(petAvatarId),
            contentDescription = stringResource(R.string.game_pet_description),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.game_waiting_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}