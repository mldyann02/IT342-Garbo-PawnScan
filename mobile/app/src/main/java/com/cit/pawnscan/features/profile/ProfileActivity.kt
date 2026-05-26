package com.cit.pawnscan.features.profile

import android.os.Bundle
import android.view.View
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
    private lateinit var profileName: TextView
    private lateinit var editPanel: View
    private lateinit var fullNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var saveButton: Button
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.profile_status)
        profileMeta = findViewById(R.id.profile_meta)
        profileName = findViewById(R.id.profile_display_name)
        editPanel = findViewById(R.id.profile_edit_panel)
        fullNameInput = findViewById(R.id.profile_full_name)
        phoneInput = findViewById(R.id.profile_phone)
        saveButton = findViewById(R.id.profile_save)
        PortalUi.configureBottomNav(this, "profile")

        saveButton.setOnClickListener {
            if (isEditing) saveProfile() else setEditing(true)
        }
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
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for profile.", true)
            }
        })
    }

    private fun renderProfile(profile: UserProfileResponse) {
        fullNameInput.setText(profile.fullName.orEmpty())
        phoneInput.setText(profile.phoneNumber ?: "+639")
        profileName.text = profile.fullName?.ifBlank { "PawnScan User" } ?: "PawnScan User"
        profileMeta.text =
            "${profile.email ?: "Unavailable"}\n${profile.phoneNumber ?: "Phone not provided"}\n${profile.role ?: "USER"} account | Member since ${PortalUi.formatDate(profile.createdAt)}"
        setEditing(false)
    }

    private fun setEditing(editing: Boolean) {
        isEditing = editing
        editPanel.visibility = if (editing) View.VISIBLE else View.GONE
        saveButton.text = if (editing) getString(R.string.portal_save_profile) else "Edit Profile"
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
