package com.furiqd.itsalive

import com.furiqd.auth.presentation.navigation.AuthGraph
import com.furiqd.home.presentation.navigation.HomeGraph
import com.furiqd.personalisation.presentation.navigation.PersonalisationGraph
import com.furiqd.shared.domain.model.UserSessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticationNavigationTest {

    @Test
    fun sessionStateNotAuthorizedResolvesToAuthGraph() {
        val destination = resolveInitialDestination(UserSessionState.NotAuthorized)
        assertEquals(AuthGraph, destination)
    }

    @Test
    fun sessionStateNeedsOnboardingResolvesToPersonalisationGraph() {
        val destination = resolveInitialDestination(UserSessionState.NeedsOnboarding)
        assertEquals(PersonalisationGraph, destination)
    }

    @Test
    fun sessionStateFullyAuthorizedResolvesToHomeGraph() {
        val destination = resolveInitialDestination(UserSessionState.FullyAuthorized)
        assertEquals(HomeGraph, destination)
    }

    private fun resolveInitialDestination(sessionState: UserSessionState): Any {
        return when (sessionState) {
            UserSessionState.NeedsOnboarding -> PersonalisationGraph
            UserSessionState.FullyAuthorized -> HomeGraph
            else -> AuthGraph
        }
    }
}
