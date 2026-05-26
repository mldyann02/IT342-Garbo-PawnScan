package com.cit.pawnscan.shared.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        // In a real production app, we would send this token to our backend server
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
        
        // Define channel ID
        val channelId = "pawnscan_alerts"
        
        // Create Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "PawnScan Match Alerts"
            val channelDesc = "Real-time match alerts for your reported items"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = channelDesc
                enableLights(true)
                lightColor = 0xFFEF4444.toInt() // Red alert light
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

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
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
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
