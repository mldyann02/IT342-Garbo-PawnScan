package com.cit.pawnscan.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object JwtStorageUtil {
    private const val PREFS_NAME = "pawnscan_secure_prefs"
    private const val JWT_TOKEN_KEY = "jwt_token"
    private const val USER_EMAIL_KEY = "user_email"
    private const val USER_ROLE_KEY = "user_role"

    private fun getEncryptedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveToken(context: Context, token: String) {
        getEncryptedPreferences(context).edit()
            .putString(JWT_TOKEN_KEY, token)
            .apply()
    }

    fun getToken(context: Context): String? {
        return getEncryptedPreferences(context).getString(JWT_TOKEN_KEY, null)
    }

    fun saveUserEmail(context: Context, email: String) {
        getEncryptedPreferences(context).edit()
            .putString(USER_EMAIL_KEY, email)
            .apply()
    }

    fun getUserEmail(context: Context): String? {
        return getEncryptedPreferences(context).getString(USER_EMAIL_KEY, null)
    }

    fun saveUserRole(context: Context, role: String) {
        getEncryptedPreferences(context).edit()
            .putString(USER_ROLE_KEY, role)
            .apply()
    }

    fun getUserRole(context: Context): String? {
        return getEncryptedPreferences(context).getString(USER_ROLE_KEY, null)
    }

    fun clearAll(context: Context) {
        getEncryptedPreferences(context).edit()
            .clear()
            .apply()
    }
}
