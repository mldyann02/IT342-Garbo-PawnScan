package com.cit.pawnscan

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.api.AuthService
import com.cit.pawnscan.api.LoginRequest
import com.cit.pawnscan.api.RetrofitClient
import com.cit.pawnscan.utils.JwtStorageUtil
import com.cit.pawnscan.utils.ValidationUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        val backButton = findViewById<Button>(R.id.back_button)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val registerLink = findViewById<TextView>(R.id.register_link)

        // Form inputs
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)

        // Password toggle button
        val btnTogglePassword = findViewById<ImageButton>(R.id.btn_toggle_password)

        // Check if user came from registration page
        val registrationEmail = intent.getStringExtra("registered_email")
        val registrationRole = intent.getStringExtra("registered_role")
        if (!registrationEmail.isNullOrEmpty()) {
            showStatusMessage(
                "Registration successful! Please log in with your credentials.",
                isError = false
            )
            emailInput.setText(registrationEmail)
        }

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Password visibility toggle
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordInput, btnTogglePassword, isPasswordVisible)
        }

        // Sign in button
        btnSignIn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            handleLogin(email, password, btnSignIn)
        }

        // Register link
        registerLink.setOnClickListener {
            navigateToRegistration()
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

    private fun handleLogin(email: String, password: String, submitButton: Button) {
        // Validate inputs
        val validationError = validateLoginForm(email, password)

        if (validationError != null) {
            showStatusMessage(validationError, isError = true)
            return
        }

        // Update button state to show loading
        submitButton.isEnabled = false
        submitButton.text = resources.getString(R.string.login_signing_in_button)

        // Prepare request
        val request = LoginRequest(
            email = email,
            password = password
        )

        // Make API call
        val authService = RetrofitClient.getAuthService()
        authService.login(request).enqueue(object : Callback<com.cit.pawnscan.api.AuthResponse> {
            override fun onResponse(
                call: Call<com.cit.pawnscan.api.AuthResponse>,
                response: Response<com.cit.pawnscan.api.AuthResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // Save token and user information
                    if (!authResponse.token.isNullOrEmpty()) {
                        JwtStorageUtil.saveToken(this@LoginActivity, authResponse.token)
                    }
                    if (!authResponse.email.isNullOrEmpty()) {
                        JwtStorageUtil.saveUserEmail(this@LoginActivity, authResponse.email)
                    }
                    if (!authResponse.role.isNullOrEmpty()) {
                        JwtStorageUtil.saveUserRole(this@LoginActivity, authResponse.role)
                    }

                    // Show success message
                    val successMsg = authResponse.message ?: "Login successful!"
                    showStatusMessage(successMsg, isError = false)

                    // Navigate to dashboard after delay
                    findViewById<TextView>(R.id.status_message).postDelayed({
                        navigateToDashboard()
                    }, 1500)
                } else {
                    // Handle error response from server
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            parseErrorMessage(errorBody)
                        } else {
                            "Login failed. Please check your credentials."
                        }
                    } catch (e: Exception) {
                        "Login failed. Please check your credentials."
                    }
                    showStatusMessage(errorMsg, isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = resources.getString(R.string.login_sign_in_button)
                }
            }

            override fun onFailure(call: Call<com.cit.pawnscan.api.AuthResponse>, t: Throwable) {
                val errorMsg = when {
                    t.message?.contains("Unable to resolve host") == true ->
                        "Network error: Cannot reach server. Check your connection."
                    t.message?.contains("timeout") == true ->
                        "Request timeout: Server took too long to respond."
                    else -> "Login failed: ${t.message ?: "Unknown error"}"
                }
                showStatusMessage(errorMsg, isError = true)
                submitButton.isEnabled = true
                submitButton.text = resources.getString(R.string.login_sign_in_button)
            }
        })
    }

    private fun validateLoginForm(email: String, password: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Please enter a valid email address"
            password.isEmpty() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val json = org.json.JSONObject(errorBody)
            json.optString("message", "Login failed. Please try again.")
        } catch (e: Exception) {
            "Login failed. Please try again."
        }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE

        if (isError) {
            statusMessage.setTextColor(resources.getColor(R.color.text_red, null))
        } else {
            statusMessage.setTextColor(resources.getColor(R.color.brand_green, null))
        }
    }

    private fun navigateToRegistration() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, TemporaryDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
