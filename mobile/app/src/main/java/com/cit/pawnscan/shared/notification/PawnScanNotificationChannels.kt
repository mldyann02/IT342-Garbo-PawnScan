package com.cit.pawnscan.shared.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PawnScanNotificationChannels {
    const val ALERTS_CHANNEL_ID = "pawnscan_alerts"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            "PawnScan Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Real-time PawnScan notification alerts"
            enableLights(true)
            lightColor = 0xFFEF4444.toInt()
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
