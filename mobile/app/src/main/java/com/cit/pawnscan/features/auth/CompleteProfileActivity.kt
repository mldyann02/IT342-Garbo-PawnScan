package com.cit.pawnscan.features.auth

import android.os.Bundle
import android.content.Intent
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.AuthResponse
import com.cit.pawnscan.features.auth.api.CompleteProfileRequest
import com.cit.pawnscan.shared.auth.AuthSessionRouter
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.validation.ValidationUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompleteProfileActivity : AppCompatActivity() {

    private val isBusiness: Boolean
        get() = JwtStorageUtil.getUserRole(this) == "BUSINESS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val subtitle = findViewById<TextView>(R.id.complete_profile_subtitle)
        val phoneInput = findViewById<EditText>(R.id.phone_input)
        val businessNameInput = findViewById<EditText>(R.id.business_name_input)
        val businessAddressInput = findViewById<EditText>(R.id.business_address_input)
        val permitNumberInput = findViewById<EditText>(R.id.permit_number_input)
        val submitButton = findViewById<Button>(R.id.btn_complete_profile)

        subtitle.text = getString(
            if (isBusiness) R.string.complete_profile_business_subtitle else R.string.complete_profile_user_subtitle
        )
        businessNameInput.visibility = if (isBusiness) View.VISIBLE else View.GONE
        businessAddressInput.visibility = if (isBusiness) View.VISIBLE else View.GONE
        permitNumberInput.visibility = if (isBusiness) View.VISIBLE else View.GONE

        phoneInput.filters = arrayOf(InputFilter.LengthFilter(13))
        phoneInput.setText("+639")
        phoneInput.setSelection(phoneInput.text.length)
        phoneInput.addTextChangedListener(PhilippinePhoneTextWatcher(phoneInput))

        backButton.setOnClickListener {
            JwtStorageUtil.clearAll(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        submitButton.setOnClickListener {
            handleCompleteProfile(
                phone = phoneInput.text.toString(),
                businessName = businessNameInput.text.toString(),
                businessAddress = businessAddressInput.text.toString(),
                permitNumber = permitNumberInput.text.toString(),
                submitButton = submitButton
            )
        }
    }

    private fun handleCompleteProfile(
        phone: String,
        businessName: String,
        businessAddress: String,
        permitNumber: String,
        submitButton: Button
    ) {
        findViewById<TextView>(R.id.status_message).visibility = View.GONE

        val normalizedPhone = ValidationUtil.normalizePhilippinePhone(phone)
        ValidationUtil.validatePhoneNumber(normalizedPhone)?.let {
            showStatusMessage(it, isError = true)
            return
        }

        if (isBusiness) {
            ValidationUtil.validateBusinessName(businessName)?.let {
                showStatusMessage(it, isError = true)
                return
            }
            ValidationUtil.validateBusinessAddress(businessAddress)?.let {
                showStatusMessage(it, isError = true)
                return
            }
            ValidationUtil.validatePermitNumber(permitNumber)?.let {
                showStatusMessage(it, isError = true)
                return
            }
        }

        val token = JwtStorageUtil.getToken(this)
        if (token.isNullOrBlank()) {
            showStatusMessage(getString(R.string.complete_profile_error_session), isError = true)
            return
        }

        submitButton.isEnabled = false
        submitButton.text = getString(R.string.complete_profile_saving_button)

        val request = CompleteProfileRequest(
            phoneNumber = normalizedPhone,
            businessName = businessName.takeIf { isBusiness },
            businessAddress = businessAddress.takeIf { isBusiness },
            permitNumber = permitNumber.takeIf { isBusiness }
        )

        RetrofitClient.getAuthService()
            .completeProfile("Bearer $token", request)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        authResponse.token?.takeIf { it.isNotBlank() }?.let {
                            JwtStorageUtil.saveToken(this@CompleteProfileActivity, it)
                        }
                        authResponse.email?.takeIf { it.isNotBlank() }?.let {
                            JwtStorageUtil.saveUserEmail(this@CompleteProfileActivity, it)
                        }
                        authResponse.role?.takeIf { it.isNotBlank() }?.let {
                            JwtStorageUtil.saveUserRole(this@CompleteProfileActivity, it)
                        }
                        showStatusMessage(
                            authResponse.message ?: getString(R.string.complete_profile_success),
                            isError = false
                        )
                        findViewById<TextView>(R.id.status_message).postDelayed({
                            AuthSessionRouter.routeAfterAuthentication(this@CompleteProfileActivity)
                        }, 700)
                        return
                    }

                    val message = AuthErrorParser.parse(
                        response.errorBody()?.string(),
                        getString(R.string.complete_profile_error_failed)
                    )
                    showStatusMessage(message, isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.complete_profile_button)
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    showStatusMessage(getString(R.string.complete_profile_error_network), isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.complete_profile_button)
                }
            })
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE
        statusMessage.setTextColor(getColor(if (isError) R.color.text_red else R.color.brand_green))
    }
}
