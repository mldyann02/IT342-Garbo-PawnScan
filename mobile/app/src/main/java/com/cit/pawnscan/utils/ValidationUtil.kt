package com.cit.pawnscan.utils

import android.util.Patterns

object ValidationUtil {
    
    fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Full name is required"
            fullName.length > 255 -> "Full name must not exceed 255 characters"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email format is invalid"
            email.length > 255 -> "Email must not exceed 255 characters"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            password.length > 100 -> "Password must not exceed 100 characters"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirm password is required"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun validatePhoneNumber(phoneNumber: String): String? {
        // Follow web validation: contact number is required and must match Philippine formats
        if (phoneNumber.isBlank()) {
            return "Contact number is required"
        }

        // Pattern: +639171234567 or 09171234567
        val pattern = Regex("^(\\+63|0)9\\d{9}$")

        return when {
            phoneNumber.length > 20 -> "Phone number must not exceed 20 characters"
            !pattern.matches(phoneNumber) -> "Contact number must be a valid Philippine mobile number (e.g. +639171234567 or 09171234567)"
            else -> null
        }
    }

    fun validateBusinessName(businessName: String): String? {
        return when {
            businessName.isBlank() -> "Business name is required"
            businessName.length > 255 -> "Business name must not exceed 255 characters"
            else -> null
        }
    }

    fun validateBusinessAddress(businessAddress: String): String? {
        return when {
            businessAddress.isBlank() -> "Business address is required"
            businessAddress.length > 2000 -> "Business address must not exceed 2000 characters"
            else -> null
        }
    }

    fun validatePermitNumber(permitNumber: String): String? {
        if (permitNumber.isBlank()) {
            return "Permit number is required"
        }
        return when {
            permitNumber.length > 100 -> "Permit number must not exceed 100 characters"
            else -> null
        }
    }

    fun normalizePhilippinePhone(input: String): String {
        val v = input.trim()
        if (v.isEmpty()) return v

        // If already in +63XXXXXXXXXX format and valid length, return as-is
        if (Regex("^\\+639\\d{9}$").matches(v)) return v

        // If local 0XXXXXXXXXX convert to +63XXXXXXXXXX
        if (Regex("^0\\d{10}$").matches(v)) return "+63" + v.substring(1)

        // If starts with 63XXXXXXXXXX (missing +), add +
        if (Regex("^63\\d{10}$").matches(v)) return "+" + v

        // If user pasted or entered +63 but with extra chars, try to extract digits
        val digits = v.replace(Regex("[^\\d]"), "")
        if (digits.length == 11 && digits.startsWith("09")) {
            return "+63" + digits.substring(1)
        }
        if (digits.length == 12 && digits.startsWith("63")) {
            return "+" + digits
        }

        // fallback to trimmed original
        return v
    }
}
