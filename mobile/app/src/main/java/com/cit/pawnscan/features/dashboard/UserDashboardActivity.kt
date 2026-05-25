package com.cit.pawnscan.features.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.reports.api.MatchedReportResponse
import com.cit.pawnscan.features.reports.api.ReportResponse
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDashboardActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var totalReports: TextView
    private lateinit var totalMatches: TextView
    private lateinit var recentReportsList: LinearLayout
    private var matchCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        if (PortalUi.requireAuth(this) == null) return

        bindViews()
        bindActions()
        PortalUi.configureBottomNav(this, "home")
        userEmail.text = JwtStorageUtil.getUserEmail(this) ?: "Signed in"
        userName.text = "PawnScan User"
        loadDashboard()
    }

    private fun bindViews() {
        statusMessage = findViewById(R.id.portal_status)
        userName = findViewById(R.id.portal_user_name)
        userEmail = findViewById(R.id.portal_user_email)
        totalReports = findViewById(R.id.dashboard_total_reports)
        totalMatches = findViewById(R.id.dashboard_total_matches)
        recentReportsList = findViewById(R.id.dashboard_recent_reports)
    }

    private fun bindActions() {
        findViewById<Button>(R.id.portal_logout).setOnClickListener { PortalUi.logout(this) }
        findViewById<Button>(R.id.dashboard_new_report).setOnClickListener { PortalUi.goCreateReport(this) }
        findViewById<Button>(R.id.dashboard_view_reports).setOnClickListener { PortalUi.goReports(this) }
        totalMatches.setOnClickListener { PortalUi.goMatches(this) }
        totalReports.setOnClickListener { PortalUi.goReports(this) }
    }

    private fun loadDashboard() {
        loadProfile()
        loadReports()
        loadMatches()
    }

    private fun loadProfile() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                val profile = response.body()
                if (response.isSuccessful && profile != null) {
                    userName.text = profile.fullName?.ifBlank { "PawnScan User" } ?: "PawnScan User"
                    userEmail.text = profile.email ?: userEmail.text
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not load profile details.", true)
            }
        })
    }

    private fun loadReports() {
        val header = PortalUi.requireAuth(this) ?: return
        PortalUi.showStatus(statusMessage, "Syncing your portal...", false)
        RetrofitClient.getReportService().getReports(header).enqueue(object : Callback<List<ReportResponse>> {
            override fun onResponse(call: Call<List<ReportResponse>>, response: Response<List<ReportResponse>>) {
                if (!response.isSuccessful) {
                    PortalUi.showStatus(statusMessage, "Unable to load reports.", true)
                    return
                }
                val reports = (response.body() ?: emptyList()).sortedByDescending { it.createdAt ?: "" }
                totalReports.text = "${reports.size}\nReports"
                renderRecentReports(reports.take(3))
                PortalUi.showStatus(statusMessage, "Portal updated.", false)
            }

            override fun onFailure(call: Call<List<ReportResponse>>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for reports.", true)
            }
        })
    }

    private fun loadMatches() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getReportService().getMatchedReports(header).enqueue(object : Callback<List<MatchedReportResponse>> {
            override fun onResponse(call: Call<List<MatchedReportResponse>>, response: Response<List<MatchedReportResponse>>) {
                if (response.isSuccessful) {
                    matchCount = response.body()?.size ?: 0
                    totalMatches.text = "$matchCount\nMatches"
                }
            }

            override fun onFailure(call: Call<List<MatchedReportResponse>>, t: Throwable) {
                totalMatches.text = "$matchCount\nMatches"
            }
        })
    }

    private fun renderRecentReports(reports: List<ReportResponse>) {
        recentReportsList.removeAllViews()
        if (reports.isEmpty()) {
            recentReportsList.addView(emptyText(getString(R.string.portal_empty_reports)))
            return
        }
        reports.forEach { report ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_user_report_card, recentReportsList, false)
            view.findViewById<TextView>(R.id.report_model).text = report.itemModel ?: "Reported item"
            view.findViewById<TextView>(R.id.report_status).text = PortalUi.statusLabel(report.status)
            view.findViewById<TextView>(R.id.report_serial).text = "SN: ${report.serialNumber ?: "Unavailable"}"
            view.findViewById<TextView>(R.id.report_description).text = report.description ?: "No description provided"
            view.findViewById<TextView>(R.id.report_date).text = "Created: ${PortalUi.formatDate(report.createdAt)}"
            view.findViewById<LinearLayout>(R.id.report_actions).visibility = View.GONE
            view.setOnClickListener { PortalUi.goReports(this) }
            recentReportsList.addView(view)
        }
    }

    private fun emptyText(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(getColor(R.color.text_muted_gray))
            textSize = 14f
            setPadding(18, 24, 18, 24)
            setBackgroundResource(R.drawable.bg_mobile_feature_card)
        }
    }
}
