package com.cit.pawnscan.features.business

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.dashboard.UserDashboardActivity
import com.cit.pawnscan.features.business.BusinessProfileActivity
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object BusinessPortalUi {
    fun requireBusiness(activity: Activity): String? {
        val header = PortalUi.requireAuth(activity) ?: return null
        if (JwtStorageUtil.getUserRole(activity) != "BUSINESS") {
            navigate(activity, UserDashboardActivity::class.java)
            activity.finish()
            return null
        }
        return header
    }

    fun requireVerifiedBusiness(activity: Activity, statusView: TextView, onVerified: () -> Unit) {
        val header = requireBusiness(activity) ?: return
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                val business = response.body()?.businessProfile
                val verified = business?.verified == true || business?.isVerified == true
                val rejected = business?.rejected == true || business?.isRejected == true
                when {
                    verified -> {
                        statusView.visibility = View.GONE
                        onVerified()
                    }
                    rejected -> PortalUi.showStatus(
                        statusView,
                        "Your business account was rejected. ${business?.rejectionReason ?: "Please contact support."}",
                        true
                    )
                    else -> PortalUi.showStatus(
                        statusView,
                        "Your business account is under review. Verification features unlock once approved.",
                        false
                    )
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusView, "Could not verify your business account status.", true)
            }
        })
    }

    fun goHome(activity: Activity) = navigate(activity, BusinessDashboardActivity::class.java)
    fun goVerify(activity: Activity) = navigate(activity, BusinessVerifyActivity::class.java)
    fun goHistory(activity: Activity) {
        navigate(activity, BusinessHistoryActivity::class.java, BusinessHistoryActivity.TAB_SEARCHES)
    }
    fun goMatches(activity: Activity) {
        navigate(activity, BusinessHistoryActivity::class.java, BusinessHistoryActivity.TAB_MATCHES)
    }
    fun goProfile(activity: Activity) = navigate(activity, BusinessProfileActivity::class.java)

    fun configureBottomNav(activity: Activity, active: String) {
        val activeKey = if (active == "matches") "history" else active
        val navItems = mapOf(
            "home" to (R.id.nav_home to R.id.nav_home_icon),
            "history" to (R.id.nav_reports to R.id.nav_reports_icon),
            "verify" to (R.id.nav_create to R.id.nav_create_icon),
            "profile" to (R.id.nav_profile to R.id.nav_profile_icon)
        )
        navItems.forEach { (key, ids) ->
            activity.findViewById<View?>(ids.first)?.apply {
                val isActive = key == activeKey
                setBackgroundResource(if (isActive) R.drawable.bg_nav_item_active else R.drawable.bg_nav_item_glass)
                activity.findViewById<ImageView?>(ids.second)?.imageTintList = ColorStateList.valueOf(
                    activity.getColor(if (isActive) R.color.bg_main_dark else R.color.text_white)
                )
                setOnClickListener {
                    if (!isActive) {
                        when (key) {
                            "home" -> goHome(activity)
                            "history" -> goHistory(activity)
                            "verify" -> goVerify(activity)
                            "profile" -> goProfile(activity)
                        }
                    }
                }
            }
        }
    }

    private fun navigate(activity: Activity, target: Class<out Activity>, tab: String? = null) {
        if (activity::class.java == target && tab == null) return
        val intent = Intent(activity, target)
        tab?.let { intent.putExtra(BusinessHistoryActivity.EXTRA_TAB, it) }
        if (tab == null) {
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}
