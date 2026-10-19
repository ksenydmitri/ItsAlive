package com.furiqd.home.presentation.screen.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.home.presentation.element.ActiveRoomCard
import com.furiqd.home.presentation.element.CreateRoomPlaceholderCard
import com.furiqd.home.presentation.element.WaitingForFriendRoomCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainState,
    onEvent: (MainEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { state.rooms.size })

    Column(modifier){
        Row(
            modifier = modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onEvent(MainEvent.ProfileClicked)} ) {
                Icon(
                    modifier = modifier.fillMaxSize(),
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile")
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            pageSpacing = 16.dp
        ) { page ->
            when (val room = state.rooms[page]) {
                is RoomUiModel.Active -> {
                    Log.d("MainScreen", "Active room: $room")
                    ActiveRoomCard(
                        room = room,
                        onCameraClick = {onEvent(MainEvent.CameraClicked)},
                        modifier = modifier,
                        onInciteFriendClick = { onEvent(MainEvent.InciteFriendClicked) }
                    )
                }
                is RoomUiModel.Placeholder -> {
                    CreateRoomPlaceholderCard(
                        onCreateRoomClick = {
                            onEvent(MainEvent.OpenCreateRoomDialogClicked) },
                        modifier = modifier
                    )
                }
                is RoomUiModel.WaitingForFriend -> {
                    WaitingForFriendRoomCard(
                        room = room,
                        onClick = { /*onEvent(MainEvent.WaitingFriendRoomClicked(room.id))*/ },
                        modifier = modifier,
                        onDeleteClick = { onEvent(MainEvent.DeleteRoomClicked(room.id)) }
                    )
                }
            }
        }
    }
}
@Composable
fun MainRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToPairing: () -> Unit,
) {
    val viewModel: MainViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.TriggerPetAnimation -> {}
                is MainEffect.CopyToClipboard -> {}
                is MainEffect.ShowToast -> {}
                is MainEffect.NavigateToAuth -> {
                    onNavigateToAuth()
                }
                is MainEffect.NavigateToProfile -> {
                    onNavigateToProfile()
                }
                is MainEffect.NavigateToPairing -> {
                    onNavigateToPairing()
                }
            }
        }
    }

    MainScreen(
        state = state,
        onEvent = viewModel::handleEvent
    )
}