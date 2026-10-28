package com.furiqd.home.presentation.screen.main

import android.net.Uri

sealed interface MainEvent {
    object OpenCreateRoomDialogClicked : MainEvent
    object PetClicked : MainEvent
    object ProfileClicked : MainEvent
    data class ChallengePhotoCaptured(val uri: Uri) : MainEvent
    data class CopyInviteCodeClicked(val code: String) : MainEvent
    data class DeleteRoomClicked(val roomId: String) : MainEvent
    object InciteFriendClicked : MainEvent
    data class PageSelected(val pageIndex: Int) : MainEvent
    data class UpdateFcmToken(val token: String) : MainEvent
}