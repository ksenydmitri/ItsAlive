package com.furiqd.home.presentation.element.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import com.furiqd.home.R
import com.furiqd.home.presentation.model.RoomUiModel
import com.furiqd.shared.ui.elements.AvatarImage

@Composable
internal fun TodayHeader(
    room: RoomUiModel,
    onProfileClick: () -> Unit
) {
    val userAvatarUrl = when (room) {
        is RoomUiModel.Active -> room.userAvatarUrl
        is RoomUiModel.WaitingForFriend -> room.userAvatarUrl
        is RoomUiModel.Placeholder -> null
    }
    val friendAvatarUrl = (room as? RoomUiModel.Active)?.friendAvatarUrl
    val streakCount = (room as? RoomUiModel.Active)?.streakCount

    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(Modifier.size(72.dp, 44.dp)) {
            if (room !is RoomUiModel.Placeholder) {
                OnlineAvatar(userAvatarUrl, Modifier.align(Alignment.TopStart))
            }
            if (room is RoomUiModel.Active) {
                OnlineAvatar(friendAvatarUrl, Modifier.align(Alignment.TopEnd))
            }
        }

        if (streakCount != null && streakCount > 0) {
            Surface(color = TodayColors.peach, shape = CircleShape) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF805E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.today_streak_days, streakCount),
                        color = Color(0xFF83412A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.main_profile_description),
                tint = TodayColors.text
            )
        }
    }
}

@Composable
private fun OnlineAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(40.dp)) {
        AvatarImage(
            avatarUrl = avatarUrl,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(10.dp)
                .border(1.5.dp, Color.White, CircleShape)
                .background(Color(0xFF5EBB8B), CircleShape)
        )
    }
}
