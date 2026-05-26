package com.cit.pawnscan.features.business

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusinessProfileActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var name: TextView
    private lateinit var meta: TextView
    private lateinit var details: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_profile)

        if (BusinessPortalUi.requireBusiness(this) == null) return

        statusMessage = findViewById(R.id.business_profile_status)
        name = findViewById(R.id.business_profile_name)
        meta = findViewById(R.id.business_profile_meta)
        details = findViewById(R.id.business_profile_details)

        BusinessPortalUi.configureBottomNav(this, "profile")
        findViewById<Button>(R.id.business_profile_logout).setOnClickListener { PortalUi.logout(this) }
        loadProfile()
    }

    private fun loadProfile() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                val profile = response.body()
                if (!response.isSuccessful || profile == null) {
                    PortalUi.showStatus(statusMessage, "Unable to load business profile.", true)
                    return
                }
                val business = profile.businessProfile
                val verified = business?.verified == true || business?.isVerified == true
                val rejected = business?.rejected == true || business?.isRejected == true
                name.text = business?.businessName?.ifBlank { "Business Portal" } ?: "Business Portal"
                meta.text = "${profile.email ?: "Email unavailable"}\n${profile.phoneNumber ?: "Phone unavailable"}"
                details.text = "Permit number\n${business?.permitNumber ?: "Not provided"}\n\nBusiness address\n${business?.businessAddress ?: "Not provided"}\n\nStatus\n${when {
                    verified -> "Verified"
                    rejected -> "Rejected"
                    else -> "Under review"
                }}"
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for profile.", true)
            }
        })
    }
}
