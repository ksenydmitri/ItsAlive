package com.furiqd.home.presentation.element

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.furiqd.home.R
import com.furiqd.shared.ui.theme.ItsAliveTheme
import com.furiqd.home.presentation.model.RoomUiModel
import com.furiqd.shared.R.drawable
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val TWO_HOURS_IN_MILLIS = 2L * 60L * 60L * 1000L
private const val TIMER_DELAY_MILLIS = 1000L

@Composable
fun WaitingForFriendRoomCard(
    room: RoomUiModel.WaitingForFriend,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var millisLeft by remember(room.expiringTime) {
        val expireTimestamp = room.expiringTime + TWO_HOURS_IN_MILLIS
        val diff = expireTimestamp - System.currentTimeMillis()
        mutableLongStateOf(if (diff > 0L) diff else 0L)
    }

    LaunchedEffect(key1 = room.expiringTime) {
        while (millisLeft > 0L) {
            delay(TIMER_DELAY_MILLIS)
            val expireTimestamp = room.expiringTime + TWO_HOURS_IN_MILLIS
            val diff = expireTimestamp - System.currentTimeMillis()
            millisLeft = if (diff > 0L) diff else 0L
        }
    }

    val formattedTimeLeft = remember(millisLeft) {
        val hours = TimeUnit.MILLISECONDS.toHours(millisLeft)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (millisLeft > 0L) {
                            stringResource(R.string.room_expires_in, formattedTimeLeft)
                        } else {
                            stringResource(R.string.room_expired)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    if (millisLeft <= 0L) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { onDeleteClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Image(
                        painter = painterResource(id = room.petAvatarId),
                        contentDescription = room.petName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            AvatarWithBorder(
                                avatarUrl = room.userAvatarUrl,
                                isDone = false,
                                modifier = modifier
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = stringResource(R.string.avatar_plus_symbol),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.waiting_for_friend_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.waiting_for_friend_subtitle, room.petName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 520)
@Composable
private fun WaitingForFriendRoomCardPreview() {
    ItsAliveTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            WaitingForFriendRoomCard(
                room = RoomUiModel.WaitingForFriend(
                    id = "test_room_id",
                    isSelected = true,
                    inviteCode = "789012",
                    petName = "Дракончик",
                    petAvatarId = drawable.blue_egg,
                    expiringTime = System.currentTimeMillis() - (15L * 60L * 10000L),
                    userAvatarUrl = null
                ),
                onClick = {},
                modifier = Modifier.fillMaxSize(),
                onDeleteClick = {}
            )
        }
    }
}