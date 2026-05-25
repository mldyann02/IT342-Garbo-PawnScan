package com.cit.pawnscan.features.auth

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.AuthResponse
import com.cit.pawnscan.features.auth.api.GoogleAuthConfigResponse
import com.cit.pawnscan.features.auth.api.GoogleAuthRequest
import com.cit.pawnscan.features.dashboard.TemporaryDashboardActivity
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoogleAuthCoordinator(
    private val activity: AppCompatActivity,
    private val button: Button,
    private val statusMessage: TextView,
    private val roleProvider: () -> String?
) {
    private val authService = RetrofitClient.getAuthService()

    fun bind() {
        button.setOnClickListener {
            beginGoogleAuth()
        }
    }

    private fun beginGoogleAuth() {
        setLoading(true)
        statusMessage.visibility = View.GONE

        authService.getGoogleAuthConfig().enqueue(object : Callback<GoogleAuthConfigResponse> {
            override fun onResponse(
                call: Call<GoogleAuthConfigResponse>,
                response: Response<GoogleAuthConfigResponse>
            ) {
                val config = response.body()
                val clientId = config?.clientId?.trim().orEmpty()

                if (!response.isSuccessful || config?.configured != true || clientId.isBlank()) {
                    showStatus(
                        AuthErrorParser.parse(
                            response.errorBody()?.string(),
                            "Google sign-in is not configured yet."
                        ),
                        isError = true
                    )
                    setLoading(false)
                    return
                }

                requestGoogleCredential(clientId)
            }

            override fun onFailure(call: Call<GoogleAuthConfigResponse>, t: Throwable) {
                showStatus(networkMessage("Google sign-in setup failed", t), isError = true)
                setLoading(false)
            }
        })
    }

    private fun requestGoogleCredential(clientId: String) {
        activity.lifecycleScope.launch {
            try {
                val idToken = getGoogleIdToken(clientId)
                exchangeGoogleToken(idToken)
            } catch (e: GetCredentialException) {
                showStatus("Google sign-in was cancelled or no Google account was available.", isError = true)
                setLoading(false)
            } catch (e: GoogleIdTokenParsingException) {
                showStatus("Google returned an invalid sign-in token.", isError = true)
                setLoading(false)
            } catch (e: Exception) {
                showStatus("Google sign-in failed. Please try again.", isError = true)
                setLoading(false)
            }
        }
    }

    private suspend fun getGoogleIdToken(clientId: String): String {
        val googleOption = GetSignInWithGoogleOption.Builder(clientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request
        )
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }

        throw IllegalStateException("Unexpected Google credential type")
    }

    private fun exchangeGoogleToken(idToken: String) {
        authService.authenticateWithGoogle(
            GoogleAuthRequest(
                token = idToken,
                role = roleProvider()
            )
        ).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    saveAuthResponse(authResponse)
                    showStatus(authResponse.message ?: "Google authentication successful.", isError = false)

                    statusMessage.postDelayed({
                        val intent = Intent(activity, TemporaryDashboardActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }, 900)
                    return
                }

                showStatus(
                    AuthErrorParser.parse(
                        response.errorBody()?.string(),
                        "Google authentication failed. Please try again."
                    ),
                    isError = true
                )
                setLoading(false)
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showStatus(networkMessage("Google authentication failed", t), isError = true)
                setLoading(false)
            }
        })
    }

    private fun saveAuthResponse(authResponse: AuthResponse) {
        if (!authResponse.token.isNullOrEmpty()) {
            JwtStorageUtil.saveToken(activity, authResponse.token)
        }
        if (!authResponse.email.isNullOrEmpty()) {
            JwtStorageUtil.saveUserEmail(activity, authResponse.email)
        }
        if (!authResponse.role.isNullOrEmpty()) {
            JwtStorageUtil.saveUserRole(activity, authResponse.role)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        button.isEnabled = !isLoading
        button.text = if (isLoading) {
            activity.getString(R.string.auth_google_connecting)
        } else {
            activity.getString(R.string.auth_continue_google)
        }
    }

    private fun showStatus(message: String, isError: Boolean) {
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE
        statusMessage.setTextColor(
            activity.getColor(if (isError) R.color.text_red else R.color.brand_green)
        )
    }

    private fun networkMessage(prefix: String, throwable: Throwable): String {
        return when {
            throwable.message?.contains("Unable to resolve host") == true ->
                "$prefix: Cannot reach server. Check your connection."
            throwable.message?.contains("timeout") == true ->
                "$prefix: Server took too long to respond."
            else -> "$prefix: ${throwable.message ?: "Unknown error"}"
        }
    }
}
