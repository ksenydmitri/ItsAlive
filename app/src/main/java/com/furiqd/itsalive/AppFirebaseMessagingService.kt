package com.furiqd.itsalive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.furiqd.shared.data.di.ApplicationScope
import com.furiqd.shared.domain.usecase.SyncFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase
    @Inject
    @ApplicationScope
    lateinit var externalScope: CoroutineScope

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        if (data.isEmpty()) return

        val type = data[PushPayloadKeys.TYPE]
        val roomId = data[PushPayloadKeys.ROOM_ID] ?: return

        when (type) {
            PushNotificationType.INCITE_FRIEND.name -> {
                showInciteNotification(roomId)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token received")
        externalScope.launch {
            runCatching {
                syncFcmTokenUseCase(token)
            }.onFailure { error ->
                Log.e(TAG, "Failed to sync refreshed FCM token", error)
            }
        }
    }

    private fun showInciteNotification(roomId: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(notificationManager)

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
        } ?: return

        val notificationId = roomId.hashCode()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setSmallIcon(com.furiqd.shared.R.drawable.green_egg)
            .setContentTitle(getString(R.string.push_incite_friend_title))
            .setContentText(getString(R.string.push_incite_friend_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID_ALERTS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    getString(R.string.notification_channel_incite_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_incite_desc)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        private const val TAG = "AppFirebaseMsgService"
        const val EXTRA_ROOM_ID = "EXTRA_ROOM_ID"
        private const val CHANNEL_ID_ALERTS = "incite_friend_channel"

    }

    private object PushPayloadKeys {
        const val ROOM_ID = "roomId"
        const val TYPE = "type"
    }

    private enum class PushNotificationType {
        INCITE_FRIEND
    }

}