package com.cit.pawnscan.features.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.LoginRequest
import com.cit.pawnscan.features.landing.MainActivity
import com.cit.pawnscan.shared.auth.AuthSessionRouter
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.validation.ValidationUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var googleAuthCoordinator: GoogleAuthCoordinator
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleAuthCoordinator.handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val btnGoogleLogin = findViewById<View>(R.id.btn_google_login)
        val registerLink = findViewById<TextView>(R.id.register_link)

        // Form inputs
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)

        // Password toggle button
        val btnTogglePassword = findViewById<ImageButton>(R.id.btn_toggle_password)

//        // Initialize Navigation Bar views
//        val navSignIn = findViewById<TextView>(R.id.nav_sign_in)
//        val navGetStarted = findViewById<Button>(R.id.nav_get_started)
//
//        navSignIn.setOnClickListener {
//            emailInput.text.clear()
//            passwordInput.text.clear()
//            emailInput.requestFocus()
//        }
//
//        // Set logic for Navigation "Get Started" (Redirects to Registration)
//        navGetStarted.setOnClickListener {
//            navigateToRegistration()
//        }
        
        // Check if user came from registration page
        val registrationEmail = intent.getStringExtra("registered_email")
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

        googleAuthCoordinator = GoogleAuthCoordinator(
            activity = this,
            button = btnGoogleLogin,
            statusMessage = findViewById(R.id.status_message),
            signInLauncher = googleSignInLauncher,
            roleProvider = { null }
        )
        googleAuthCoordinator.bind()

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
        authService.login(request).enqueue(object : Callback<com.cit.pawnscan.features.auth.api.AuthResponse> {
            override fun onResponse(
                call: Call<com.cit.pawnscan.features.auth.api.AuthResponse>,
                response: Response<com.cit.pawnscan.features.auth.api.AuthResponse>
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
                        AuthSessionRouter.routeAfterAuthentication(this@LoginActivity)
                    }, 1500)
                } else {
                    // Handle error response from server
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            val parsedMsg = AuthErrorParser.parse(errorBody, "Please check your credentials and try again.")
                            "Login failed: $parsedMsg"
                        } else {
                            "Login failed: Please check your credentials and try again."
                        }
                    } catch (e: Exception) {
                        "Login failed: Please check your credentials and try again."
                    }
                    showStatusMessage(errorMsg, isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = resources.getString(R.string.login_sign_in_button)
                }
            }

            override fun onFailure(call: Call<com.cit.pawnscan.features.auth.api.AuthResponse>, t: Throwable) {
                val errorMsg = "Login failed: We could not reach the server. Please try again."
                showStatusMessage(errorMsg, isError = true)
                submitButton.isEnabled = true
                submitButton.text = resources.getString(R.string.login_sign_in_button)
            }
        })
    }

    private fun validateLoginForm(email: String, password: String): String? {
        val emailError = ValidationUtil.validateEmail(email)
        val passwordError = ValidationUtil.validatePassword(password)
        if (emailError != null || passwordError != null) {
            return "Login failed: Please check your credentials and try again."
        }
        return null
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

}


