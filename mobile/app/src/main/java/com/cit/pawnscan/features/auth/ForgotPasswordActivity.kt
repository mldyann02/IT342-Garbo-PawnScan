package com.cit.pawnscan.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.ForgotPasswordRequest
import com.cit.pawnscan.features.auth.api.MessageResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val btnSendReset = findViewById<Button>(R.id.btn_send_reset)
        val signInLink = findViewById<TextView>(R.id.sign_in_link)

        backButton.setOnClickListener { finish() }

        signInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnSendReset.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showStatusMessage(getString(R.string.forgot_password_invalid_email), isError = true)
                return@setOnClickListener
            }
            handleForgotPassword(email, btnSendReset)
        }
    }

    private fun handleForgotPassword(email: String, submitButton: Button) {
        submitButton.isEnabled = false
        submitButton.text = getString(R.string.forgot_password_sending_button)

        RetrofitClient.getAuthService()
            .forgotPassword(ForgotPasswordRequest(email = email))
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    // Always show success message regardless of whether the email exists
                    // This prevents email enumeration attacks
                    showStatusMessage(
                        getString(R.string.forgot_password_success),
                        isError = false
                    )
                    submitButton.isEnabled = false
                    submitButton.text = getString(R.string.forgot_password_send_button)
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    showStatusMessage(
                        getString(R.string.forgot_password_error_network),
                        isError = true
                    )
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.forgot_password_send_button)
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
