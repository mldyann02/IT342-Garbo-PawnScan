package com.cit.pawnscan.features.business

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.dashboard.NotificationsActivity
import com.cit.pawnscan.features.business.api.SearchLogResponse
import com.cit.pawnscan.features.business.api.StolenMatchResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusinessDashboardActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var businessName: TextView
    private lateinit var businessMeta: TextView
    private lateinit var recentList: LinearLayout
    private lateinit var verifyButton: Button
    private lateinit var viewHistory: TextView
    private val recentSearches = mutableListOf<SearchLogResponse>()
    private val stolenMatches = mutableListOf<StolenMatchResponse>()
    private var isVerifiedBusiness = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_dashboard)

        if (BusinessPortalUi.requireBusiness(this) == null) return

        statusMessage = findViewById(R.id.business_status)
        businessName = findViewById(R.id.business_name)
        businessMeta = findViewById(R.id.business_meta)
        recentList = findViewById(R.id.business_recent_searches)
        verifyButton = findViewById(R.id.business_verify_now)
        viewHistory = findViewById(R.id.business_view_history)

        BusinessPortalUi.configureBottomNav(this, "home")
        findViewById<ImageButton>(R.id.business_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        verifyButton.isEnabled = false
        viewHistory.isEnabled = false
        updateVerifyButtonState(false)
        verifyButton.setOnClickListener {
            if (isVerifiedBusiness) {
                BusinessPortalUi.goVerify(this)
            } else {
                PortalUi.showStatus(
                    statusMessage,
                    "Your business account is under review. Verification features unlock once approved.",
                    false
                )
            }
        }
        viewHistory.setOnClickListener {
            if (isVerifiedBusiness) {
                BusinessPortalUi.goHistory(this)
            } else {
                PortalUi.showStatus(
                    statusMessage,
                    "Your business account is under review. Verification features unlock once approved.",
                    false
                )
            }
        }

        loadProfile()
        refreshVerificationAccess()
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        if (::recentList.isInitialized) {
            refreshVerificationAccess()
        }
    }

    private fun loadProfile() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                val profile = response.body()
                if (response.isSuccessful && profile != null) {
                    val business = profile.businessProfile
                    businessName.text = business?.businessName?.ifBlank { "Business Portal" } ?: "Business Portal"
                    val verification = if (business?.verified == true || business?.isVerified == true) "Verified" else "Under review"
                    businessMeta.text = "${profile.email ?: "Signed in"} | $verification"
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                businessName.text = "Business Portal"
                businessMeta.text = "Profile unavailable"
            }
        })
    }

    private fun loadRecentSearches() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getVerificationService().getSearchHistory(header, 0, 6)
            .enqueue(object : Callback<List<SearchLogResponse>> {
                override fun onResponse(call: Call<List<SearchLogResponse>>, response: Response<List<SearchLogResponse>>) {
                    if (!response.isSuccessful) {
                        PortalUi.showStatus(statusMessage, "Unable to load recent verifications.", true)
                        return
                    }
                    recentSearches.clear()
                    recentSearches.addAll(response.body().orEmpty())
                    renderRecent(recentSearches)
                    statusMessage.visibility = View.GONE
                    loadStolenMatches()
                }

                override fun onFailure(call: Call<List<SearchLogResponse>>, t: Throwable) {
                    PortalUi.showStatus(statusMessage, "Could not reach the server for verification history.", true)
                }
            })
    }

    private fun refreshVerificationAccess() {
        isVerifiedBusiness = false
        verifyButton.isEnabled = false
        viewHistory.isEnabled = false
        updateVerifyButtonState(false)
        recentList.removeAllViews()
        BusinessPortalUi.requireVerifiedBusiness(this, statusMessage) {
            isVerifiedBusiness = true
            verifyButton.isEnabled = true
            viewHistory.isEnabled = true
            updateVerifyButtonState(true)
            statusMessage.visibility = View.GONE
            loadRecentSearches()
        }
    }

    private fun updateVerifyButtonState(enabled: Boolean) {
        verifyButton.setBackgroundResource(
            if (enabled) R.drawable.button_rounded_pill_green else R.drawable.button_rounded_pill_disabled
        )
        verifyButton.setTextColor(getColor(if (enabled) R.color.bg_main_dark else R.color.text_muted_gray))
    }

    private fun loadStolenMatches() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getVerificationService().getStolenMatches(header, 0, 100)
            .enqueue(object : Callback<List<StolenMatchResponse>> {
                override fun onResponse(call: Call<List<StolenMatchResponse>>, response: Response<List<StolenMatchResponse>>) {
                    if (response.isSuccessful) {
                        stolenMatches.clear()
                        stolenMatches.addAll(response.body().orEmpty())
                        renderRecent(recentSearches)
                    }
                }

                override fun onFailure(call: Call<List<StolenMatchResponse>>, t: Throwable) = Unit
            })
    }

    private fun renderRecent(searches: List<SearchLogResponse>) {
        recentList.removeAllViews()
        if (searches.isEmpty()) {
            recentList.addView(emptyText("No verifications yet."))
            return
        }
        searches.forEach { search ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_business_search_card, recentList, false)
            view.findViewById<TextView>(R.id.search_serial).text = search.searchedSerial ?: "Unknown serial"
            view.findViewById<TextView>(R.id.search_model).text = search.itemModel ?: "Model unavailable"
            view.findViewById<TextView>(R.id.search_date).text = PortalUi.formatDate(search.timestamp)
            configureVerificationBadge(view.findViewById(R.id.search_result), search.result)
            if (search.result == "STOLEN") {
                view.setOnClickListener { BusinessMatchDetailActivity.openFromSearch(this, search, stolenMatches) }
            }
            recentList.addView(view)
        }
    }

    private fun configureVerificationBadge(view: TextView, result: String?) {
        val isStolen = result == "STOLEN"
        view.text = if (isStolen) "Stolen Match" else "Clean"
        view.setBackgroundResource(if (isStolen) R.drawable.badge_status_rejected else R.drawable.badge_status_approved)
        view.setTextColor(getColor(if (isStolen) R.color.text_red else R.color.brand_green))
    }

    private fun emptyText(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(getColor(R.color.text_muted_gray))
            textSize = 14f
            setPadding(18, 24, 18, 24)
            setBackgroundResource(R.drawable.bg_glass_panel_soft)
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
