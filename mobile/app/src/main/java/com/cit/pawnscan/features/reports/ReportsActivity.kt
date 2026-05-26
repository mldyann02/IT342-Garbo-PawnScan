package com.cit.pawnscan.features.reports

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.reports.api.ReportResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportsActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var reportsList: LinearLayout
    private lateinit var searchInput: TextView
    private lateinit var filterAll: TextView
    private lateinit var filterPending: TextView
    private lateinit var filterApproved: TextView
    private lateinit var filterRejected: TextView
    private val reports = mutableListOf<ReportResponse>()
    private var activeStatus = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.reports_status)
        reportsList = findViewById(R.id.reports_list)
        searchInput = findViewById(R.id.reports_search)
        filterAll = findViewById(R.id.reports_filter_all)
        filterPending = findViewById(R.id.reports_filter_pending)
        filterApproved = findViewById(R.id.reports_filter_approved)
        filterRejected = findViewById(R.id.reports_filter_rejected)
        PortalUi.configureBottomNav(this, "reports")

        findViewById<Button>(R.id.reports_new).setOnClickListener { PortalUi.goCreateReport(this) }
        bindFilters()
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderReports()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        loadReports()
    }

    override fun onResume() {
        super.onResume()
        if (::reportsList.isInitialized) loadReports()
    }

    private fun loadReports() {
        val header = PortalUi.requireAuth(this) ?: return
        PortalUi.showStatus(statusMessage, "Loading your reports...", false)
        RetrofitClient.getReportService().getReports(header).enqueue(object : Callback<List<ReportResponse>> {
            override fun onResponse(call: Call<List<ReportResponse>>, response: Response<List<ReportResponse>>) {
                if (!response.isSuccessful) {
                    PortalUi.showStatus(statusMessage, "Unable to load reports.", true)
                    return
                }
                reports.clear()
                reports.addAll((response.body() ?: emptyList()).sortedByDescending { it.createdAt ?: "" })
                renderReports()
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<ReportResponse>>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for reports.", true)
            }
        })
    }

    private fun renderReports() {
        reportsList.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()
        val visibleReports = reports.filter { report ->
            val matchesStatus = activeStatus == "ALL" || (report.status ?: "APPROVED") == activeStatus
            val haystack = listOf(report.itemModel, report.serialNumber, report.description, report.status)
                .joinToString(" ")
                .lowercase()
            matchesStatus && (query.isBlank() || haystack.contains(query))
        }
        if (visibleReports.isEmpty()) {
            reportsList.addView(emptyText(getString(R.string.portal_empty_reports)))
            return
        }
        visibleReports.forEach { report ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_user_report_card, reportsList, false)
            view.findViewById<TextView>(R.id.report_model).text = report.itemModel ?: "Reported item"
            PortalUi.configureStatusBadge(view.findViewById(R.id.report_status), report.status)
            view.findViewById<TextView>(R.id.report_serial).text = "SN: ${report.serialNumber ?: "Unavailable"}"
            view.findViewById<TextView>(R.id.report_description).text = "Evidence: ${report.files?.size ?: 0} file(s)"
            view.findViewById<TextView>(R.id.report_date).text = "Created: ${PortalUi.formatDate(report.createdAt)}"
            view.findViewById<Button>(R.id.report_edit).setOnClickListener { openEdit(report) }
            view.findViewById<ImageButton>(R.id.report_delete).setOnClickListener { confirmDelete(report) }
            view.setOnClickListener { openDetail(report) }
            reportsList.addView(view)
        }
    }

    private fun bindFilters() {
        val tabs = mapOf(
            "ALL" to filterAll,
            "PENDING" to filterPending,
            "APPROVED" to filterApproved,
            "REJECTED" to filterRejected
        )
        tabs.forEach { (status, tab) ->
            tab.setOnClickListener {
                activeStatus = status
                tabs.forEach { (key, view) ->
                    val selected = key == activeStatus
                    view.setBackgroundResource(if (selected) R.drawable.bg_status_tab_active else R.drawable.bg_status_tab_inactive)
                    view.setTextColor(getColor(if (selected) R.color.brand_green else R.color.text_light_gray))
                }
                renderReports()
            }
        }
    }

    private fun openDetail(report: ReportResponse) {
        val intent = Intent(this, ReportDetailActivity::class.java)
        ReportDetailActivity.putReport(intent, report)
        startActivity(intent)
    }

    private fun openEdit(report: ReportResponse) {
        val id = report.id ?: return
        val intent = Intent(this, ReportFormActivity::class.java)
        intent.putExtra(ReportFormActivity.EXTRA_REPORT_ID, id)
        intent.putExtra(ReportFormActivity.EXTRA_SERIAL, report.serialNumber.orEmpty())
        intent.putExtra(ReportFormActivity.EXTRA_MODEL, report.itemModel.orEmpty())
        intent.putExtra(ReportFormActivity.EXTRA_DESCRIPTION, report.description.orEmpty())
        intent.putExtra(ReportFormActivity.EXTRA_STATUS, report.status.orEmpty())
        startActivity(intent)
    }

    private fun confirmDelete(report: ReportResponse) {
        val id = report.id ?: return
        AlertDialog.Builder(this)
            .setTitle("Delete report?")
            .setMessage("This will permanently delete ${report.itemModel ?: "this report"}.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteReport(id) }
            .show()
    }

    private fun deleteReport(id: Long) {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getReportService().deleteReport(header, id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    reports.removeAll { it.id == id }
                    renderReports()
                    PortalUi.showStatus(statusMessage, "Report deleted.", false)
                } else {
                    PortalUi.showStatus(statusMessage, "Unable to delete report.", true)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server while deleting.", true)
            }
        })
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
