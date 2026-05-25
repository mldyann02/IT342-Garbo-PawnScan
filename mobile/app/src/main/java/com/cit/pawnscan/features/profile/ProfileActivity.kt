package com.cit.pawnscan.features.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.auth.api.UserProfileUpdateRequest
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var profileMeta: TextView
    private lateinit var fullNameInput: EditText
    private lateinit var phoneInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.profile_status)
        profileMeta = findViewById(R.id.profile_meta)
        fullNameInput = findViewById(R.id.profile_full_name)
        phoneInput = findViewById(R.id.profile_phone)
        PortalUi.configureBottomNav(this, "profile")

        findViewById<Button>(R.id.profile_save).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.profile_logout).setOnClickListener { PortalUi.logout(this) }
        loadProfile()
    }

    private fun loadProfile() {
        val header = PortalUi.requireAuth(this) ?: return
        PortalUi.showStatus(statusMessage, "Loading profile...", false)
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                val profile = response.body()
                if (!response.isSuccessful || profile == null) {
                    PortalUi.showStatus(statusMessage, "Unable to load your profile.", true)
                    return
                }
                renderProfile(profile)
                PortalUi.showStatus(statusMessage, "Profile loaded.", false)
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for profile.", true)
            }
        })
    }

    private fun renderProfile(profile: UserProfileResponse) {
        fullNameInput.setText(profile.fullName.orEmpty())
        phoneInput.setText(profile.phoneNumber ?: "+639")
        profileMeta.text =
            "Account: ${profile.role ?: "USER"}\nEmail: ${profile.email ?: "Unavailable"}\nMember since: ${PortalUi.formatDate(profile.createdAt)}"
    }

    private fun saveProfile() {
        val fullName = fullNameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        if (fullName.isBlank()) {
            PortalUi.showStatus(statusMessage, "Full name is required.", true)
            return
        }
        if (!Regex("^\\+639\\d{9}$").matches(phone)) {
            PortalUi.showStatus(statusMessage, "Use a valid Philippine mobile number, e.g. +639171234567.", true)
            return
        }

        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getAuthService()
            .updateProfile(header, UserProfileUpdateRequest(fullName, phone))
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    val updated = response.body()
                    if (response.isSuccessful && updated != null) {
                        renderProfile(updated)
                        PortalUi.showStatus(statusMessage, "Profile updated successfully.", false)
                    } else {
                        PortalUi.showStatus(statusMessage, "Unable to save profile.", true)
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    PortalUi.showStatus(statusMessage, "Could not reach the server while saving profile.", true)
                }
            })
    }
}
