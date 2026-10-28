package com.furiqd.home.presentation.screen.main

import androidx.annotation.StringRes

sealed interface MainEffect {
    data class CopyToClipboard(val text: String) : MainEffect
    data class ShowToast(@StringRes val messageRes: Int) : MainEffect
    object NavigateToProfile : MainEffect
    data class NavigateToPetDashboard(val roomId: String) : MainEffect
    object NavigateToPairing : MainEffect
}