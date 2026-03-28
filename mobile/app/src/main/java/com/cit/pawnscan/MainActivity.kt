package com.cit.pawnscan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
            // TODO: Navigate to report stolen item screen
            navigateToReportStolen()
        }

        btnBusinessVerification.setOnClickListener {
            // TODO: Navigate to business verification screen
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
        // TODO: Implement navigation to report stolen item screen
    }

    private fun navigateToBusinessVerification() {
        // TODO: Implement navigation to business verification screen
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
