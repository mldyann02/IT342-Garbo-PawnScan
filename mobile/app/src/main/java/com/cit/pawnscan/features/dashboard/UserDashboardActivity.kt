package com.cit.pawnscan.features.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var recentReportsList: RecyclerView
    private lateinit var recentReportsAdapter: RecentReportsAdapter
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
        totalReports.text = "0\nReports"
        totalMatches.text = "0\nMatches"
        loadDashboard()
    }

    private fun bindViews() {
        statusMessage = findViewById(R.id.portal_status)
        userName = findViewById(R.id.portal_user_name)
        userEmail = findViewById(R.id.portal_user_email)
        totalReports = findViewById(R.id.dashboard_total_reports)
        totalMatches = findViewById(R.id.dashboard_total_matches)
        recentReportsList = findViewById(R.id.dashboard_recent_reports)
        
        recentReportsAdapter = RecentReportsAdapter(emptyList()) { report ->
            PortalUi.goReports(this)
        }
        recentReportsList.layoutManager = LinearLayoutManager(this)
        recentReportsList.adapter = recentReportsAdapter
    }

    private fun bindActions() {
        findViewById<android.widget.ImageButton>(R.id.portal_notifications).setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
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
                statusMessage.visibility = View.GONE
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
        recentReportsAdapter.updateReports(reports)
    }

    // emptyText method removed as empty state is better handled outside or within the adapter, but for now we simply show an empty list.
}
