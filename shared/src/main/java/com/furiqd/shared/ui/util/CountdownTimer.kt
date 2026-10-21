package com.furiqd.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TIMER_DELAY_MILLIS = 1000L
private const val TWO_HOURS_IN_MILLIS = 2 * 60 * 60 * 1000L

@Composable
fun rememberCountdownTimer(
    targetTimeMillis: Long,
    addedDurationMillis: Long = TWO_HOURS_IN_MILLIS
): String {
    var millisLeft by remember(targetTimeMillis, addedDurationMillis) {
        val expireTimestamp = targetTimeMillis + addedDurationMillis
        val diff = expireTimestamp - System.currentTimeMillis()
        mutableLongStateOf(diff.coerceAtLeast(0L))
    }

    LaunchedEffect(targetTimeMillis, addedDurationMillis) {
        while (millisLeft > 0L) {
            delay(TIMER_DELAY_MILLIS)
            val expireTimestamp = targetTimeMillis + addedDurationMillis
            val diff = expireTimestamp - System.currentTimeMillis()
            millisLeft = diff.coerceAtLeast(0L)
        }
    }

    return remember(millisLeft) {
        val hours = TimeUnit.MILLISECONDS.toHours(millisLeft)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60

        if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}