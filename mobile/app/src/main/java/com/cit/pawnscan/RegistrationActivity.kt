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
import com.cit.pawnscan.api.RegisterRequest
import com.cit.pawnscan.api.RetrofitClient
import com.cit.pawnscan.utils.JwtStorageUtil
import com.cit.pawnscan.utils.ValidationUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {
    
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isBusinessMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize views
        val backButton = findViewById<Button>(R.id.back_button)
        val btnIndividual = findViewById<Button>(R.id.btn_individual)
        val btnBusiness = findViewById<Button>(R.id.btn_business)
        val btnCreateAccount = findViewById<Button>(R.id.btn_create_account)
        val signInLink = findViewById<TextView>(R.id.sign_in_link)
        
        // Password toggle buttons
        val btnTogglePassword = findViewById<ImageButton>(R.id.btn_toggle_password)
        val btnToggleConfirmPassword = findViewById<ImageButton>(R.id.btn_toggle_confirm_password)

        // Form inputs
        val fullNameInput = findViewById<EditText>(R.id.full_name_input)
        val businessNameInput = findViewById<EditText>(R.id.business_name_input)
        val businessAddressInput = findViewById<EditText>(R.id.business_address_input)
        val permitNumberInput = findViewById<EditText>(R.id.permit_number_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val phoneInput = findViewById<EditText>(R.id.phone_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_input)

        // Restrict phone input to digits and optional leading +, and limit length similar to web
        phoneInput.filters = arrayOf(android.text.InputFilter.LengthFilter(13))
        phoneInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                s ?: return
                var v = s.toString()
                // Remove invalid characters (allow digits and plus)
                v = v.replace(Regex("[^\\d+]") , "")
                // Allow only a single leading plus
                if (v.count { it == '+' } > 1) {
                    v = v.replace("+", "")
                    v = "+" + v
                }
                // If plus is not leading, remove it
                if (v.contains('+') && !v.startsWith('+')) {
                    v = v.replace("+", "")
                }
                if (v != s.toString()) {
                    phoneInput.setText(v)
                    phoneInput.setSelection(v.length.coerceAtMost(v.length))
                }
            }
        })

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Account type toggle - default Individual
        updateAccountTypeUI(btnIndividual, btnBusiness)

        btnIndividual.setOnClickListener {
            isBusinessMode = false
            updateAccountTypeUI(btnIndividual, btnBusiness)
            updateFieldVisibility(
                fullNameInput, businessNameInput, businessAddressInput, permitNumberInput
            )
        }

        btnBusiness.setOnClickListener {
            isBusinessMode = true
            updateAccountTypeUI(btnBusiness, btnIndividual)
            updateFieldVisibility(
                fullNameInput, businessNameInput, businessAddressInput, permitNumberInput
            )
        }

        // Password visibility toggle
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordInput, btnTogglePassword, isPasswordVisible)
        }

        // Confirm password visibility toggle
        btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(
                confirmPasswordInput,
                btnToggleConfirmPassword,
                isConfirmPasswordVisible
            )
        }

        // Create account button
        btnCreateAccount.setOnClickListener {
            val fullName = if (!isBusinessMode) fullNameInput.text.toString() else businessNameInput.text.toString()
            val businessName = if (isBusinessMode) businessNameInput.text.toString() else ""
            val businessAddress = if (isBusinessMode) businessAddressInput.text.toString() else ""
            val permitNumber = if (isBusinessMode) permitNumberInput.text.toString() else ""
            val email = emailInput.text.toString()
            val phone = phoneInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            handleRegistration(
                fullName,
                businessName,
                businessAddress,
                permitNumber,
                email,
                phone,
                password,
                confirmPassword,
                btnCreateAccount
            )
        }

        // Sign in link
        signInLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateAccountTypeUI(activeButton: Button, inactiveButton: Button) {
        val textWhiteColor = resources.getColor(R.color.text_white, null)
        val bgMainDarkColor = resources.getColor(R.color.bg_main_dark, null)

        // Choose left/right pill shapes based on which button is active
        if (activeButton.id == R.id.btn_individual) {
            activeButton.setBackgroundResource(R.drawable.toggle_left_active)
            inactiveButton.setBackgroundResource(R.drawable.toggle_right_inactive)
        } else {
            activeButton.setBackgroundResource(R.drawable.toggle_right_active)
            inactiveButton.setBackgroundResource(R.drawable.toggle_left_inactive)
        }

        activeButton.setTextColor(bgMainDarkColor)
        inactiveButton.setTextColor(textWhiteColor)
    }

    private fun updateFieldVisibility(
        fullNameInput: EditText,
        businessNameInput: EditText,
        businessAddressInput: EditText,
        permitNumberInput: EditText
    ) {
        if (isBusinessMode) {
            fullNameInput.visibility = View.GONE
            businessNameInput.visibility = View.VISIBLE
            businessAddressInput.visibility = View.VISIBLE
            permitNumberInput.visibility = View.VISIBLE

            // Clear individual mode field
            fullNameInput.text.clear()
        } else {
            fullNameInput.visibility = View.VISIBLE
            businessNameInput.visibility = View.GONE
            businessAddressInput.visibility = View.GONE
            permitNumberInput.visibility = View.GONE

            // Clear business mode fields
            businessNameInput.text.clear()
            businessAddressInput.text.clear()
            permitNumberInput.text.clear()
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

    private fun handleRegistration(
        fullName: String,
        businessName: String,
        businessAddress: String,
        permitNumber: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        submitButton: Button
    ) {
        // Clear previous message
        val statusMessage = findViewById<TextView>(R.id.status_message)
        statusMessage.visibility = View.GONE

        // Validate inputs
        val validationError = validateRegistrationForm(
            fullName,
            businessName,
            businessAddress,
            permitNumber,
            email,
            phone,
            password,
            confirmPassword
        )

        if (validationError != null) {
            showStatusMessage(validationError, isError = true)
            return
        }

        // Normalize phone number
        val normalizedPhone = if (phone.isNotBlank()) {
            ValidationUtil.normalizePhilippinePhone(phone)
        } else {
            ""
        }

        // Update button state to show loading
        submitButton.isEnabled = false
        submitButton.text = resources.getString(R.string.registration_registering_button)

        // Prepare request
        val role = if (isBusinessMode) "BUSINESS" else "USER"
        val request = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            phoneNumber = if (normalizedPhone.isNotBlank()) normalizedPhone else null,
            businessName = if (isBusinessMode) businessName else null,
            businessAddress = if (isBusinessMode) businessAddress else null,
            permitNumber = if (isBusinessMode) permitNumber else null,
            role = role
        )

        // Make API call
        val authService = RetrofitClient.getAuthService()
        authService.register(request).enqueue(object : Callback<com.cit.pawnscan.api.AuthResponse> {
            override fun onResponse(
                call: Call<com.cit.pawnscan.api.AuthResponse>,
                response: Response<com.cit.pawnscan.api.AuthResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    
                    // Save token and user information
                    if (!authResponse.token.isNullOrEmpty()) {
                        JwtStorageUtil.saveToken(this@RegistrationActivity, authResponse.token)
                    }
                    if (!authResponse.email.isNullOrEmpty()) {
                        JwtStorageUtil.saveUserEmail(this@RegistrationActivity, authResponse.email)
                    }
                    if (!authResponse.role.isNullOrEmpty()) {
                        JwtStorageUtil.saveUserRole(this@RegistrationActivity, authResponse.role)
                    }

                    // Show success message
                    val successMsg = authResponse.message ?: "Registration successful!"
                    showStatusMessage(successMsg, isError = false)

                    // Navigate to login after delay (pass registered email and role)
                    findViewById<TextView>(R.id.status_message).postDelayed({
                        navigateToLogin(authResponse.email, authResponse.role)
                    }, 1500)
                } else {
                    // Handle error response from server
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            parseErrorMessage(errorBody)
                        } else {
                            "Registration failed. Please try again."
                        }
                    } catch (e: Exception) {
                        "Registration failed. Please check your details."
                    }
                    showStatusMessage(errorMsg, isError = true)
                    submitButton.isEnabled = true
                    submitButton.text = resources.getString(R.string.registration_cta_button)
                }
            }

            override fun onFailure(call: Call<com.cit.pawnscan.api.AuthResponse>, t: Throwable) {
                val errorMsg = when {
                    t.message?.contains("Unable to resolve host") == true ->
                        "Network error: Cannot reach server. Check your connection."
                    t.message?.contains("timeout") == true ->
                        "Request timeout: Server took too long to respond."
                    else -> "Registration failed: ${t.message ?: "Unknown error"}"
                }
                showStatusMessage(errorMsg, isError = true)
                submitButton.isEnabled = true
                submitButton.text = resources.getString(R.string.registration_cta_button)
            }
        })
    }

    private fun validateRegistrationForm(
        fullName: String,
        businessName: String,
        businessAddress: String,
        permitNumber: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): String? {
        // Validate email (always required)
        ValidationUtil.validateEmail(email)?.let { return it }

        // Validate password (always required)
        ValidationUtil.validatePassword(password)?.let { return it }

        // Validate confirm password
        ValidationUtil.validateConfirmPassword(password, confirmPassword)?.let { return it }

        // Validate phone (required per web)
        ValidationUtil.validatePhoneNumber(phone)?.let { return it }

        if (isBusinessMode) {
            // Validate business fields
            ValidationUtil.validateBusinessName(businessName)?.let { return it }
            ValidationUtil.validateBusinessAddress(businessAddress)?.let { return it }
            ValidationUtil.validatePermitNumber(permitNumber)?.let { return it }
        } else {
            // Validate individual fields
            ValidationUtil.validateFullName(fullName)?.let { return it }
        }

        return null // All validations passed
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

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            // Try to parse JSON error response
            if (errorBody.contains("message")) {
                val messageIndex = errorBody.indexOf("\"message\":")
                val startIndex = errorBody.indexOf("\"", messageIndex + 10) + 1
                val endIndex = errorBody.indexOf("\"", startIndex)
                if (startIndex > 0 && endIndex > startIndex) {
                    return errorBody.substring(startIndex, endIndex)
                }
            }
            "Registration failed. Please try again."
        } catch (e: Exception) {
            "Registration failed. Please try again."
        }
    }

    private fun navigateToLogin(registeredEmail: String? = null, registeredRole: String? = null) {
        val intent = Intent(this, LoginActivity::class.java)
        if (!registeredEmail.isNullOrEmpty()) intent.putExtra("registered_email", registeredEmail)
        if (!registeredRole.isNullOrEmpty()) intent.putExtra("registered_role", registeredRole)
        startActivity(intent)
        finish()
    }
}


