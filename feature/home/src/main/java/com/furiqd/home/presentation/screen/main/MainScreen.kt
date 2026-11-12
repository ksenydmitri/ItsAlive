package com.furiqd.home.presentation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.home.presentation.element.today.MainBottomNavigation
import com.furiqd.home.presentation.element.today.TodayColors
import com.furiqd.home.presentation.model.RoomUiModel
import com.furiqd.shared.ui.elements.rememberCameraLauncher

@Composable
fun MainScreen(
    state: MainState,
    onEvent: (MainEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { state.rooms.size })
    LaunchedEffect(pagerState.currentPage) {
        onEvent(MainEvent.PageSelected(pagerState.currentPage))
    }
    val launchCamera = rememberCameraLauncher(
        onPhotoCaptured = { uri -> onEvent(MainEvent.ChallengePhotoCaptured(uri)) }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TodayColors.background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TodayColors.coral)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                state.rooms.getOrNull(page)?.let { room ->
                    TodayPage(
                        room = room,
                        onEvent = onEvent,
                        onCameraClick = launchCamera
                    )
                }
            }
        }

        MainBottomNavigation(
            onTodayClick = {},
            onPetClick = {
                when (val room = state.selectedRoom) {
                    is RoomUiModel.Active -> onEvent(MainEvent.PetClicked(room.id))
                    is RoomUiModel.WaitingForFriend -> onEvent(MainEvent.PetClicked(room.id))
                    is RoomUiModel.Placeholder, null ->
                        onEvent(MainEvent.OpenCreateRoomDialogClicked)
                }
            },
            onPairClick = { onEvent(MainEvent.OpenCreateRoomDialogClicked) }
        )
    }
}

@Composable
fun MainRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToPetDashboard: (roomId: String) -> Unit
) {
    val viewModel: MainViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.NavigateToPetDashboard ->
                    onNavigateToPetDashboard(state.selectedRoom?.id.orEmpty())
                is MainEffect.CopyToClipboard -> Unit
                is MainEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
                is MainEffect.NavigateToProfile -> onNavigateToProfile()
                is MainEffect.NavigateToPairing -> onNavigateToPairing()
            }
        }
    }

    MainScreen(state = state, onEvent = viewModel::handleEvent)
}