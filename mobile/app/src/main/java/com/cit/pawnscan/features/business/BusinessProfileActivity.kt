package com.cit.pawnscan.features.business

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

class BusinessProfileActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var name: TextView
    private lateinit var meta: TextView
    private lateinit var details: TextView
    private lateinit var editPanel: View
    private lateinit var businessNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var permitInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var saveButton: Button
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_profile)

        if (BusinessPortalUi.requireBusiness(this) == null) return

        statusMessage = findViewById(R.id.business_profile_status)
        name = findViewById(R.id.business_profile_name)
        meta = findViewById(R.id.business_profile_meta)
        details = findViewById(R.id.business_profile_details)
        editPanel = findViewById(R.id.business_profile_edit_panel)
        businessNameInput = findViewById(R.id.business_profile_name_input)
        phoneInput = findViewById(R.id.business_profile_phone_input)
        permitInput = findViewById(R.id.business_profile_permit_input)
        addressInput = findViewById(R.id.business_profile_address_input)
        saveButton = findViewById(R.id.business_profile_save)

        BusinessPortalUi.configureBottomNav(this, "profile")
        saveButton.setOnClickListener {
            if (isEditing) saveProfile() else setEditing(true)
        }
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
                renderProfile(profile)
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for profile.", true)
            }
        })
    }

    private fun renderProfile(profile: UserProfileResponse) {
        val business = profile.businessProfile
        val verified = business?.verified == true || business?.isVerified == true
        val rejected = business?.rejected == true || business?.isRejected == true
        val displayName = business?.businessName?.ifBlank { profile.fullName ?: "Business Portal" } ?: "Business Portal"
        val phone = profile.phoneNumber ?: "+639"
        val permit = business?.permitNumber.orEmpty()
        val address = business?.businessAddress.orEmpty()

        businessNameInput.setText(displayName)
        phoneInput.setText(phone)
        permitInput.setText(permit)
        addressInput.setText(address)
        name.text = displayName
        meta.text = "${profile.email ?: "Email unavailable"}\n${profile.phoneNumber ?: "Phone unavailable"}"
        details.text = "Permit number\n${permit.ifBlank { "Not provided" }}\n\nBusiness address\n${address.ifBlank { "Not provided" }}\n\nStatus\n${when {
            verified -> "Verified"
            rejected -> "Rejected"
            else -> "Under review"
        }}"
        setEditing(false)
    }

    private fun setEditing(editing: Boolean) {
        isEditing = editing
        editPanel.visibility = if (editing) View.VISIBLE else View.GONE
        saveButton.text = if (editing) getString(R.string.portal_save_profile) else "Edit Profile"
    }

    private fun saveProfile() {
        val businessName = businessNameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val permit = permitInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        when {
            businessName.isBlank() -> {
                PortalUi.showStatus(statusMessage, "Business name is required.", true)
                return
            }
            !Regex("^(\\+63|0)9\\d{9}$").matches(phone) -> {
                PortalUi.showStatus(statusMessage, "Use a valid Philippine mobile number, e.g. +639171234567.", true)
                return
            }
            permit.isBlank() -> {
                PortalUi.showStatus(statusMessage, "Permit number is required.", true)
                return
            }
            address.isBlank() -> {
                PortalUi.showStatus(statusMessage, "Business address is required.", true)
                return
            }
        }

        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getAuthService()
            .updateProfile(
                header,
                UserProfileUpdateRequest(
                    fullName = businessName,
                    phoneNumber = phone,
                    businessName = businessName,
                    businessAddress = address,
                    permitNumber = permit
                )
            )
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    val updated = response.body()
                    if (response.isSuccessful && updated != null) {
                        renderProfile(updated)
                        PortalUi.showStatus(statusMessage, "Business profile updated successfully.", false)
                    } else {
                        PortalUi.showStatus(statusMessage, "Unable to save business profile.", true)
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    PortalUi.showStatus(statusMessage, "Could not reach the server while saving profile.", true)
                }
            })
    }
}
