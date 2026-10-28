package com.furiqd.home.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph

@Serializable
data object MainDestination

@Serializable
data object ProfileDestination

@Serializable
data class PetDashboardDestination(val roomId: String)