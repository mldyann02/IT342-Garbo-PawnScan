package com.cit.pawnscan.features.landing

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.dashboard.UserDashboardActivity
import com.cit.pawnscan.shared.auth.JwtStorageUtil

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds, then launch Landing Activity
        Handler(Looper.getMainLooper()).postDelayed({
            val target = if (JwtStorageUtil.getToken(this).isNullOrBlank()) {
                MainActivity::class.java
            } else {
                UserDashboardActivity::class.java
            }
            startActivity(Intent(this, target))
            finish() // Prevents user from going back to splash screen
        }, 2000)
    }
}
