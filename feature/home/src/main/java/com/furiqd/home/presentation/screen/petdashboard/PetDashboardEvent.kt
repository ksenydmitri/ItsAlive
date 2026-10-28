package com.furiqd.home.presentation.screen.petdashboard

sealed interface PetDashboardEvent {
    object FeedPet : PetDashboardEvent
    object PlayWithPet : PetDashboardEvent
    object Refresh : PetDashboardEvent
}
