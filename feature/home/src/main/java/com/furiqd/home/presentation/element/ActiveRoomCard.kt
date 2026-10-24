package com.furiqd.home.presentation.element

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.furiqd.home.R
import com.furiqd.home.presentation.screen.main.RoomUiModel
import com.furiqd.shared.ui.util.rememberCountdownTimer

/**
 * 4 Понятных и взаимоисключающих состояния карточки челленджа
 */
private sealed interface ChallengeDisplayState {
    // 1. Задание неактивно / ждём следующий час
    data object InactiveWaitingHour : ChallengeDisplayState

    // 2. Очередь пользователя сделать фото
    data class UserTurn(val timeLeft: Long) : ChallengeDisplayState

    // 3. Пользователь уже сделал фото, ждем друга
    data class WaitingFriend(val timeLeft: Long) : ChallengeDisplayState
    data object CompositeReady : ChallengeDisplayState
}

@Composable
fun ActiveRoomCard(
    room: RoomUiModel.Active,
    onCameraClick: () -> Unit,
    onInciteFriendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val challenge = room.challengeUiModel

    val displayState = remember(challenge) {
        when {
            challenge == null -> ChallengeDisplayState.InactiveWaitingHour
            challenge.isUserDone && challenge.isFriendDone -> ChallengeDisplayState.CompositeReady
            !challenge.isUserDone -> ChallengeDisplayState.UserTurn(timeLeft = challenge.expiresAt ?: 0L)
            else -> ChallengeDisplayState.WaitingFriend(timeLeft = challenge.expiresAt ?: 0L)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Шапка со стриком и аватарками
            StreakHeader(room = room)

            Spacer(modifier = Modifier.height(16.dp))

            // Внутренний контейнер с контентом состояния
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (displayState) {
                        is ChallengeDisplayState.UserTurn -> {
                            UserTurnContent(
                                expiringTime = displayState.timeLeft,
                                userPrompt = challenge?.userTask.orEmpty(),
                                friendPrompt = challenge?.friendTask.orEmpty(),
                                isFriendCompleted = challenge?.isFriendDone ?: false,
                                onCameraClick = onCameraClick
                            )
                        }

                        is ChallengeDisplayState.WaitingFriend -> {
                            WaitingFriendContent(
                                expiringTime = displayState.timeLeft,
                                userPrompt = challenge?.userTask.orEmpty(),
                                friendPrompt = challenge?.friendTask.orEmpty(),
                                onInciteFriendClick = onInciteFriendClick
                            )
                        }

                        is ChallengeDisplayState.CompositeReady -> {
                            CompositeReadyContent()
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

// ==========================================
// СОСТОЯНИЕ 1: ОЧЕРЕДЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
// ==========================================
@Composable
private fun UserTurnContent(
    expiringTime: Long,
    userPrompt: String,
    friendPrompt: String,
    isFriendCompleted: Boolean,
    onCameraClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimerBadge(expiringTime = expiringTime)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskItemCard(
                title = stringResource(R.string.task_label_user),
                prompt = userPrompt,
                isCompleted = false,
                isHighlight = true
            )

            TaskItemCard(
                title = stringResource(R.string.task_label_friend),
                prompt = friendPrompt,
                isCompleted = isFriendCompleted,
                isHighlight = false
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
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
    expiringTime: Long,
    userPrompt: String,
    friendPrompt: String,
    onInciteFriendClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimerBadge(expiringTime = expiringTime)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskItemCard(
                title = stringResource(R.string.task_label_user),
                prompt = userPrompt,
                isCompleted = true,
                isHighlight = false
            )

            TaskItemCard(
                title = stringResource(R.string.task_label_friend),
                prompt = friendPrompt,
                isCompleted = false,
                isHighlight = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onInciteFriendClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.action_incite_friend),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun CompositeReadyContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.game_composite_ready),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ваш общий кадр сформирован и сохранён в галерее комнаты!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InactiveHourContent(petAvatarId: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(petAvatarId),
            contentDescription = stringResource(R.string.game_pet_description),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.game_waiting_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// ВСПОМОГАТЕЛЬНЫЕ UI-КОМПОНЕНТЫ
// ==========================================

@Composable
private fun TimerBadge(expiringTime: Long) {
    val formattedTime = rememberCountdownTimer(targetTimeMillis = expiringTime)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$formattedTime ${stringResource(R.string.game_time_left_suffix)}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TaskItemCard(
    title: String,
    prompt: String,
    isCompleted: Boolean,
    isHighlight: Boolean
) {
    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isHighlight -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StreakHeader(room: RoomUiModel.Active) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (room.isStreakActive) Color(0xFFFFEAE2)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AvatarWithBorder(
            avatarUrl = room.userAvatarUrl,
            isDone = room.challengeUiModel?.isUserDone ?: false,
            modifier = Modifier.size(36.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = stringResource(R.string.streak_description),
                tint = if (room.isStreakActive) Color(0xFFFF5722) else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(R.string.streak_counter_active, room.streakCount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (room.isStreakActive) Color(0xFFFF5722) else Color.Gray
            )
        }

        AvatarWithBorder(
            avatarUrl = room.friendAvatarUrl,
            isDone = room.challengeUiModel?.isFriendDone ?: false,
            modifier = Modifier.size(36.dp)
        )
    }
}