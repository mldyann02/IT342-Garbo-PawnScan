package com.cit.pawnscan.features.auth

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.BuildConfig
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.AuthResponse
import com.cit.pawnscan.features.auth.api.GoogleAuthConfigResponse
import com.cit.pawnscan.features.auth.api.GoogleAuthRequest
import com.cit.pawnscan.shared.auth.AuthSessionRouter
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.notification.FcmTokenRegistrar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoogleAuthCoordinator(
    private val activity: AppCompatActivity,
    private val button: View,
    private val statusMessage: TextView,
    private val signInLauncher: ActivityResultLauncher<Intent>,
    private val roleProvider: () -> String?
) {
    private val authService = RetrofitClient.getAuthService()

    fun bind() {
        button.setOnClickListener {
            beginGoogleAuth()
        }
    }

    fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                showStatus("Google did not return an ID token. Check the OAuth client configuration.", isError = true)
                setLoading(false)
                return
            }

            exchangeGoogleToken(idToken)
        } catch (e: ApiException) {
            showStatus(googleSignInErrorMessage(e.statusCode), isError = true)
            setLoading(false)
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

                if (!isGoogleClientId(clientId)) {
                    showStatus(
                        "Google sign-in is misconfigured. Set GOOGLE_WEB_CLIENT_ID to a valid Web OAuth client ID.",
                        isError = true
                    )
                    setLoading(false)
                    return
                }

                launchGoogleSignIn(clientId)
            }

            override fun onFailure(call: Call<GoogleAuthConfigResponse>, t: Throwable) {
                showStatus(networkMessage("Google sign-in setup failed", t), isError = true)
                setLoading(false)
            }
        })
    }

    private fun launchGoogleSignIn(clientId: String) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientId)
            .build()
        val client = GoogleSignIn.getClient(activity, options)

        client.signOut().addOnCompleteListener {
            signInLauncher.launch(client.signInIntent)
        }.addOnFailureListener { error ->
            showStatus(networkMessage("Google sign-in could not start", error), isError = true)
            setLoading(false)
        }
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
                    FcmTokenRegistrar.registerCurrentToken(activity)
                    showStatus(authResponse.message ?: "Google authentication successful.", isError = false)

                    statusMessage.postDelayed({
                        if (authResponse.registrationStatus == "INCOMPLETE") {
                            val intent = Intent(activity, CompleteProfileActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            activity.startActivity(intent)
                            activity.finish()
                        } else {
                            AuthSessionRouter.routeAfterAuthentication(activity)
                        }
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
        val textStr = if (isLoading) {
            activity.getString(R.string.auth_google_connecting)
        } else {
            activity.getString(R.string.auth_continue_google)
        }
        if (button is TextView) {
            button.text = textStr
        } else {
            val tv = button.findViewById<TextView>(R.id.google_button_text)
            tv?.text = textStr
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

    private fun isGoogleClientId(clientId: String): Boolean {
        return clientId.endsWith(".apps.googleusercontent.com")
    }

    private fun googleSignInErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            10 -> "Google setup mismatch. Add an Android OAuth client for ${BuildConfig.APPLICATION_ID} with this debug SHA-1, and use the Web client ID on the backend."
            12501 -> "Google sign-in was cancelled."
            12500 -> "Google sign-in failed. Check Google Play services and OAuth consent setup."
            else -> "Google sign-in failed ($statusCode). Please try again."
        }
    }
}
