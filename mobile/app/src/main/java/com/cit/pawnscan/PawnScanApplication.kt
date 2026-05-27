package com.cit.pawnscan

import android.app.Application
import com.cit.pawnscan.shared.notification.PawnScanNotificationChannels

class PawnScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PawnScanNotificationChannels.create(this)
    }
}
