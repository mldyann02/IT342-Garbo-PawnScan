package com.cit.pawnscan.shared.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cit.pawnscan.R
import com.cit.pawnscan.features.dashboard.NotificationsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PawnScanMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token registered: $token")
        FcmTokenRegistrar.registerToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Retrieve notification title and body
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "PawnScan Stolen Match Alert"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "A search matches your reported item!"

        showSystemNotification(title, message)
    }

    private fun showSystemNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        PawnScanNotificationChannels.create(this)

        // Set up the click intent to go to the Notifications screen
        val intent = Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, PawnScanNotificationChannels.ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        // Show the notification with a unique ID
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "PawnScanFCM"
    }
}
