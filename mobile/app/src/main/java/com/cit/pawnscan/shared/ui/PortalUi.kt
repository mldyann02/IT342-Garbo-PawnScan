package com.cit.pawnscan.shared.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.TextView
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.LoginActivity
import com.cit.pawnscan.features.dashboard.UserDashboardActivity
import com.cit.pawnscan.features.matches.MatchedReportsActivity
import com.cit.pawnscan.features.profile.ProfileActivity
import com.cit.pawnscan.features.reports.ReportFormActivity
import com.cit.pawnscan.features.reports.ReportsActivity
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import java.text.SimpleDateFormat
import java.util.Locale

object PortalUi {
    fun authHeader(activity: Activity): String? {
        val token = JwtStorageUtil.getToken(activity) ?: return null
        return "Bearer $token"
    }

    fun requireAuth(activity: Activity): String? {
        val header = authHeader(activity)
        if (header == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
        return header
    }

    fun logout(activity: Activity) {
        JwtStorageUtil.clearAll(activity)
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun goHome(activity: Activity) {
        navigate(activity, UserDashboardActivity::class.java)
    }

    fun goReports(activity: Activity) {
        navigate(activity, ReportsActivity::class.java)
    }

    fun goCreateReport(activity: Activity) {
        navigate(activity, ReportFormActivity::class.java)
    }

    fun goMatches(activity: Activity) {
        navigate(activity, MatchedReportsActivity::class.java)
    }

    fun goProfile(activity: Activity) {
        navigate(activity, ProfileActivity::class.java)
    }

    fun configureBottomNav(activity: Activity, active: String) {
        val navItems = mapOf(
            "home" to R.id.nav_home,
            "reports" to R.id.nav_reports,
            "new" to R.id.nav_create,
            "matches" to R.id.nav_matches,
            "profile" to R.id.nav_profile
        )
        navItems.forEach { (key, id) ->
            activity.findViewById<TextView?>(id)?.apply {
                setTextColor(activity.getColor(if (key == active) R.color.brand_green else R.color.text_muted_gray))
                setOnClickListener {
                    when (key) {
                        "home" -> goHome(activity)
                        "reports" -> goReports(activity)
                        "new" -> goCreateReport(activity)
                        "matches" -> goMatches(activity)
                        "profile" -> goProfile(activity)
                    }
                }
            }
        }
    }

    fun showStatus(view: TextView, message: String, isError: Boolean) {
        view.text = message
        view.visibility = View.VISIBLE
        view.setTextColor(view.context.getColor(if (isError) R.color.text_red else R.color.brand_green))
    }

    fun formatDate(value: String?): String {
        if (value.isNullOrBlank()) return "Not available"
        val normalized = value.substringBefore(".")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return try {
            formatter.format(parser.parse(normalized)!!)
        } catch (e: Exception) {
            value.replace("T", " ")
        }
    }

    fun statusLabel(status: String?): String {
        return when (status) {
            "PENDING" -> "Pending Review"
            "REJECTED" -> "Rejected"
            "APPROVED" -> "Approved"
            else -> "Approved"
        }
    }

    fun displayName(activity: Activity, uri: Uri): String {
        activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return "evidence-${System.currentTimeMillis()}"
    }

    private fun navigate(activity: Activity, target: Class<out Activity>) {
        if (activity::class.java == target) return
        activity.startActivity(Intent(activity, target))
    }
}
