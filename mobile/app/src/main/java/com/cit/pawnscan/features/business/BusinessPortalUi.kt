package com.cit.pawnscan.features.business

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import com.cit.pawnscan.R
import com.cit.pawnscan.features.profile.ProfileActivity
import com.cit.pawnscan.shared.ui.PortalUi

object BusinessPortalUi {
    fun requireBusiness(activity: Activity): String? {
        val header = PortalUi.requireAuth(activity) ?: return null
        return header
    }

    fun goHome(activity: Activity) = navigate(activity, BusinessDashboardActivity::class.java)
    fun goVerify(activity: Activity) = navigate(activity, BusinessDashboardActivity::class.java)
    fun goHistory(activity: Activity) = navigate(activity, BusinessDashboardActivity::class.java)
    fun goMatches(activity: Activity) = navigate(activity, BusinessDashboardActivity::class.java)
    fun goProfile(activity: Activity) = navigate(activity, ProfileActivity::class.java)

    fun configureBottomNav(activity: Activity, active: String) {
        val navItems = mapOf(
            "home" to (R.id.nav_home to R.id.nav_home_icon),
            "history" to (R.id.nav_reports to R.id.nav_reports_icon),
            "verify" to (R.id.nav_create to R.id.nav_create_icon),
            "matches" to (R.id.nav_matches to R.id.nav_matches_icon),
            "profile" to (R.id.nav_profile to R.id.nav_profile_icon)
        )
        navItems.forEach { (key, ids) ->
            activity.findViewById<View?>(ids.first)?.apply {
                val isActive = key == active
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
                            "matches" -> goMatches(activity)
                            "profile" -> goProfile(activity)
                        }
                    }
                }
            }
        }
    }

    private fun navigate(activity: Activity, target: Class<out Activity>) {
        if (activity::class.java == target) return
        val intent = Intent(activity, target)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}
