package com.cit.pawnscan.features.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.AuthResponse
import com.cit.pawnscan.features.auth.api.VerifyOtpRequest
import com.cit.pawnscan.shared.auth.AuthSessionRouter
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.notification.FcmTokenRegistrar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        val role = intent.getStringExtra(EXTRA_ROLE).orEmpty()
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val emailText = findViewById<TextView>(R.id.otp_email_text)
        val otpInput = findViewById<EditText>(R.id.otp_code_input)
        val verifyButton = findViewById<Button>(R.id.btn_verify_account)
        val signInLink = findViewById<TextView>(R.id.sign_in_link)

        emailText.text = if (email.isBlank()) {
            getString(R.string.otp_missing_email)
        } else {
            getString(R.string.otp_sent_to_email, email)
        }

        otpInput.filters = arrayOf(InputFilter.LengthFilter(6))
        otpInput.inputType = InputType.TYPE_CLASS_NUMBER

        backButton.setOnClickListener {
            navigateToRegistration(role)
        }

        verifyButton.setOnClickListener {
            handleVerifyOtp(email, otpInput.text.toString().trim(), verifyButton)
        }

        signInLink.setOnClickListener {
            navigateToLogin(email)
        }
    }

    private fun handleVerifyOtp(email: String, code: String, submitButton: Button) {
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.visibility = View.GONE

        if (email.isBlank()) {
            showStatusMessage(getString(R.string.otp_error_missing_email), isError = true)
            return
        }

        if (!Regex("^\\d{6}$").matches(code)) {
            showStatusMessage(getString(R.string.otp_error_invalid_code), isError = true)
            return
        }

        submitButton.isEnabled = false
        submitButton.text = getString(R.string.otp_verifying_button)

        RetrofitClient.getAuthService()
            .verifyOtp(VerifyOtpRequest(email = email, code = code))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        saveAuthResponse(authResponse, email)
                        FcmTokenRegistrar.registerCurrentToken(this@VerifyOtpActivity)
                        showStatusMessage(authResponse.message ?: getString(R.string.otp_success), isError = false)
                        findViewById<TextView>(R.id.status_message).postDelayed({
                            AuthSessionRouter.routeAfterAuthentication(this@VerifyOtpActivity)
                        }, 900)
                        return
                    }

                    val message = AuthErrorParser.parse(
                        response.errorBody()?.string(),
                        getString(R.string.otp_error_failed)
                    )
                    showStatusMessage(message, isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.otp_verify_button)
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    showStatusMessage(getString(R.string.otp_error_network), isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.otp_verify_button)
                }
            })
    }

    private fun saveAuthResponse(authResponse: AuthResponse, fallbackEmail: String) {
        authResponse.token?.takeIf { it.isNotBlank() }?.let {
            JwtStorageUtil.saveToken(this, it)
        }
        (authResponse.email ?: fallbackEmail).takeIf { it.isNotBlank() }?.let {
            JwtStorageUtil.saveUserEmail(this, it)
        }
        authResponse.role?.takeIf { it.isNotBlank() }?.let {
            JwtStorageUtil.saveUserRole(this, it)
        }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE
        statusMessage.setTextColor(getColor(if (isError) R.color.text_red else R.color.brand_green))
    }

    private fun navigateToRegistration(role: String) {
        val intent = Intent(this, RegistrationActivity::class.java)
        if (role == "BUSINESS") intent.putExtra("account_type", role)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin(email: String) {
        val intent = Intent(this, LoginActivity::class.java)
        if (email.isNotBlank()) intent.putExtra("registered_email", email)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_ROLE = "extra_role"
    }
}
