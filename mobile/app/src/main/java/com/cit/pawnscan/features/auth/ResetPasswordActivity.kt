package com.cit.pawnscan.features.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.MessageResponse
import com.cit.pawnscan.features.auth.api.ResetPasswordRequest
import com.cit.pawnscan.shared.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val token = intent.getStringExtra(EXTRA_TOKEN).orEmpty()

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val newPasswordInput = findViewById<EditText>(R.id.new_password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_input)
        val btnToggleNew = findViewById<ImageButton>(R.id.btn_toggle_new_password)
        val btnToggleConfirm = findViewById<ImageButton>(R.id.btn_toggle_confirm_password)
        val btnResetPassword = findViewById<Button>(R.id.btn_reset_password)
        val signInLink = findViewById<TextView>(R.id.sign_in_link)

        if (token.isBlank()) {
            showStatusMessage(getString(R.string.reset_password_invalid_token), isError = true)
            btnResetPassword.isEnabled = false
        }

        backButton.setOnClickListener { finish() }

        signInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnToggleNew.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(newPasswordInput, btnToggleNew, isNewPasswordVisible)
        }

        btnToggleConfirm.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordInput, btnToggleConfirm, isConfirmPasswordVisible)
        }

        btnResetPassword.setOnClickListener {
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            handleResetPassword(token, newPassword, confirmPassword, btnResetPassword)
        }
    }

    private fun togglePasswordVisibility(input: EditText, button: ImageButton, isVisible: Boolean) {
        if (isVisible) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            button.setImageResource(R.drawable.ic_eye_off)
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye_open)
        }
        input.setSelection(input.text.length)
    }

    private fun handleResetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String,
        submitButton: Button
    ) {
        if (newPassword.length < 8) {
            showStatusMessage(getString(R.string.reset_password_error_length), isError = true)
            return
        }

        if (newPassword != confirmPassword) {
            showStatusMessage(getString(R.string.reset_password_error_mismatch), isError = true)
            return
        }

        submitButton.isEnabled = false
        submitButton.text = getString(R.string.reset_password_resetting_button)

        RetrofitClient.getAuthService()
            .resetPassword(ResetPasswordRequest(token = token, newPassword = newPassword))
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    if (response.isSuccessful) {
                        showStatusMessage(getString(R.string.reset_password_success), isError = false)
                        // Navigate to login after a short delay
                        findViewById<TextView>(R.id.status_message).postDelayed({
                            val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }, 2000)
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrEmpty()) {
                                AuthErrorParser.parse(errorBody, getString(R.string.reset_password_error_failed))
                            } else {
                                getString(R.string.reset_password_error_failed)
                            }
                        } catch (e: Exception) {
                            getString(R.string.reset_password_error_failed)
                        }
                        showStatusMessage(errorMsg, isError = true)
                        submitButton.isEnabled = true
                        submitButton.text = getString(R.string.reset_password_button)
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    showStatusMessage(getString(R.string.reset_password_error_network), isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = getString(R.string.reset_password_button)
                }
            })
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE
        statusMessage.setTextColor(getColor(if (isError) R.color.text_red else R.color.brand_green))
    }

    companion object {
        const val EXTRA_TOKEN = "extra_reset_token"
    }
}
