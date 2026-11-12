package com.furiqd.local.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.furiqd.local.receiver.StreakExpirationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StreakExpirationSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
): StreakExpirationScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleExpiration(roomId: String, expiresAtMillis: Long) {
        val intent = Intent(context, StreakExpirationReceiver::class.java).apply {
            putExtra("EXTRA_ROOM_ID", roomId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            expiresAtMillis,
            pendingIntent
        )
    }

    override fun cancelExpiration(roomId: String) {
        val intent = Intent(context, StreakExpirationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}