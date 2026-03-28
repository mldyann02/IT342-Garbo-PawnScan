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
            // TODO: Navigate to login screen
            navigateToLogin()
        }

        navGetStarted.setOnClickListener {
            // TODO: Navigate to registration or appropriate screen
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
        // TODO: Implement navigation to login screen
    }

    private fun navigateToGetStarted() {
        // Navigate to registration screen
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}