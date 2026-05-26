package com.cit.pawnscan.features.dashboard

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
