package com.cit.pawnscan.features.reports

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
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
    private val reports = mutableListOf<ReportResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.reports_status)
        reportsList = findViewById(R.id.reports_list)
        PortalUi.configureBottomNav(this, "reports")

        findViewById<Button>(R.id.reports_new).setOnClickListener { PortalUi.goCreateReport(this) }
        findViewById<Button>(R.id.reports_refresh).setOnClickListener { loadReports() }
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
        if (reports.isEmpty()) {
            reportsList.addView(emptyText(getString(R.string.portal_empty_reports)))
            return
        }
        reports.forEach { report ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_user_report_card, reportsList, false)
            view.findViewById<TextView>(R.id.report_model).text = report.itemModel ?: "Reported item"
            view.findViewById<TextView>(R.id.report_status).text = PortalUi.statusLabel(report.status)
            view.findViewById<TextView>(R.id.report_serial).text = "SN: ${report.serialNumber ?: "Unavailable"}"
            view.findViewById<TextView>(R.id.report_description).text = report.description ?: "No description provided"
            view.findViewById<TextView>(R.id.report_date).text = "Created: ${PortalUi.formatDate(report.createdAt)}"
            view.findViewById<Button>(R.id.report_edit).setOnClickListener { openEdit(report) }
            view.findViewById<Button>(R.id.report_delete).setOnClickListener { confirmDelete(report) }
            reportsList.addView(view)
        }
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
