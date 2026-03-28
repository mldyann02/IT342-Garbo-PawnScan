package com.cit.pawnscan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.utils.JwtStorageUtil

class TemporaryDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temporary_dashboard)

        val welcomeTitle = findViewById<TextView>(R.id.dashboard_welcome_title)
        val userEmail = findViewById<TextView>(R.id.dashboard_user_email)
        val userRole = findViewById<TextView>(R.id.dashboard_user_role)
        val logoutButton = findViewById<Button>(R.id.btn_logout)

        val email = JwtStorageUtil.getUserEmail(this) ?: "Unknown user"
        val role = JwtStorageUtil.getUserRole(this) ?: "USER"

        welcomeTitle.text = getString(R.string.dashboard_welcome_title)
        userEmail.text = getString(R.string.dashboard_user_email, email)
        userRole.text = getString(R.string.dashboard_user_role, role)

        logoutButton.setOnClickListener {
            JwtStorageUtil.clearAll(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}