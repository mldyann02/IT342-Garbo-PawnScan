package com.cit.pawnscan.features.landing

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
// Ensure these imports match your new package structure
import com.cit.pawnscan.features.auth.LoginActivity
import com.cit.pawnscan.features.auth.RegistrationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Initialize views
        val btnReportStolen = findViewById<Button>(R.id.btn_report_stolen)
        val btnBusinessVerification = findViewById<Button>(R.id.btn_business_verification)
        val navSignIn = findViewById<TextView>(R.id.nav_sign_in)
        val navGetStarted = findViewById<Button>(R.id.nav_get_started)

        // Set click listeners
        btnReportStolen.setOnClickListener {
            navigateToReportStolen()
        }

        btnBusinessVerification.setOnClickListener {
            navigateToBusinessVerification()
        }

        navSignIn.setOnClickListener {
            navigateToLogin()
        }

        navGetStarted.setOnClickListener {
            navigateToGetStarted()
        }
    }

    private fun navigateToReportStolen() {
        // Redirecting to Registration as per the web landing page logic
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToBusinessVerification() {
        // Redirecting to Registration for business as per web logic
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToGetStarted() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}