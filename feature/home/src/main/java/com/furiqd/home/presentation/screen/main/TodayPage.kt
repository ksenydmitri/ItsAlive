package com.furiqd.home.presentation.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furiqd.home.R
import com.furiqd.home.presentation.element.today.BuddyQuickVisitCard
import com.furiqd.home.presentation.element.today.DailyChallengeCard
import com.furiqd.home.presentation.element.today.TodayColors
import com.furiqd.home.presentation.element.today.TodayHeader
import com.furiqd.home.presentation.model.RoomUiModel

@Composable
internal fun TodayPage(
    room: RoomUiModel,
    onEvent: (MainEvent) -> Unit,
    onCameraClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TodayHeader(
            room = room,
            onProfileClick = { onEvent(MainEvent.ProfileClicked) }
        )
        TodayGreeting()

        when (room) {
            is RoomUiModel.Active -> {
                DailyChallengeCard(
                    room = room,
                    onCameraClick = onCameraClick,
                    onInciteFriendClick = { onEvent(MainEvent.InciteFriendClicked) },
                    onPetClick = { onEvent(MainEvent.PetClicked(room.id)) }
                )
                BuddyQuickVisitCard(
                    room = room,
                    onClick = { onEvent(MainEvent.PetClicked(room.id)) }
                )
            }
            is RoomUiModel.WaitingForFriend -> {
                RoomPromptCard(
                    title = stringResource(R.string.today_room_ready_title),
                    message = stringResource(R.string.today_room_ready_message, room.inviteCode),
                    action = stringResource(R.string.today_pair_action),
                    onClick = { onEvent(MainEvent.OpenCreateRoomDialogClicked) },
                    onDelete = { onEvent(MainEvent.DeleteRoomClicked(room.id)) }
                )
            }
            is RoomUiModel.Placeholder -> {
                RoomPromptCard(
                    title = stringResource(R.string.today_start_title),
                    message = stringResource(R.string.today_start_message),
                    action = stringResource(R.string.today_get_started),
                    onClick = { onEvent(MainEvent.OpenCreateRoomDialogClicked) }
                )
            }
        }
    }
}

@Composable
private fun TodayGreeting() {
    Column {
        Text(
            text = stringResource(R.string.today_greeting_title),
            color = TodayColors.text,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.today_greeting_subtitle),
            color = TodayColors.subtleText,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun RoomPromptCard(
    title: String,
    message: String,
    action: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = TodayColors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.today_delete_room)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = TodayColors.subtleText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = TodayColors.coral)
            ) {
                Text(action)
            }
        }
    }
}
