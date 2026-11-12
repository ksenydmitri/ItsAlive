package com.furiqd.local.scheduler

interface StreakExpirationScheduler {
    fun scheduleExpiration(roomId: String, expiresAtMillis: Long)
    fun cancelExpiration(roomId: String)
}