package com.cit.pawnscan.features.landing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.shared.auth.AuthSessionRouter

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AuthSessionRouter.routeFromSplash(this)
    }
}
