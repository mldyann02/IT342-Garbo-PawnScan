package com.cit.pawnscan.features.reports

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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

class ReportDetailActivity : AppCompatActivity() {
    private var reportId: Long = -1L
    private var model: String = ""
    private var serial: String = ""
    private var description: String = ""
    private var status: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        findViewById<ImageButton>(R.id.report_detail_back).setOnClickListener { finish() }

        reportId = intent.getLongExtra(EXTRA_ID, -1L)
        model = intent.getStringExtra(EXTRA_MODEL).orEmpty().ifBlank { "Report details" }
        status = intent.getStringExtra(EXTRA_STATUS).orEmpty()
        serial = intent.getStringExtra(EXTRA_SERIAL).orEmpty()
        description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
        val rejection = intent.getStringExtra(EXTRA_REJECTION).orEmpty()

        findViewById<ImageButton>(R.id.report_detail_edit).setOnClickListener { openEdit() }
        findViewById<ImageButton>(R.id.report_detail_delete).setOnClickListener { confirmDelete() }

        findViewById<TextView>(R.id.report_detail_title).text = model
        PortalUi.configureStatusBadge(findViewById(R.id.report_detail_status), status)
        findViewById<TextView>(R.id.report_detail_serial).text =
            "Serial number\n${serial.ifBlank { "Unavailable" }}"
        findViewById<TextView>(R.id.report_detail_date).text =
            "Created\n${PortalUi.formatDate(intent.getStringExtra(EXTRA_CREATED_AT))}"
        findViewById<TextView>(R.id.report_detail_description).text =
            description.ifBlank { "No description provided" }
        findViewById<TextView>(R.id.report_detail_files).text =
            "Evidence attached: ${intent.getIntExtra(EXTRA_FILE_COUNT, 0)} file(s)"

        findViewById<TextView>(R.id.report_detail_rejection).apply {
            visibility = if (status == "REJECTED" && rejection.isNotBlank()) View.VISIBLE else View.GONE
            text = "Rejection reason\n$rejection"
        }
    }

    private fun openEdit() {
        if (reportId <= 0) return
        val intent = Intent(this, ReportFormActivity::class.java)
        intent.putExtra(ReportFormActivity.EXTRA_REPORT_ID, reportId)
        intent.putExtra(ReportFormActivity.EXTRA_SERIAL, serial)
        intent.putExtra(ReportFormActivity.EXTRA_MODEL, model)
        intent.putExtra(ReportFormActivity.EXTRA_DESCRIPTION, description)
        intent.putExtra(ReportFormActivity.EXTRA_STATUS, status)
        startActivity(intent)
    }

    private fun confirmDelete() {
        if (reportId <= 0) return
        AlertDialog.Builder(this)
            .setTitle("Delete report?")
            .setMessage("This will permanently delete ${model.ifBlank { "this report" }}.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteReport() }
            .show()
    }

    private fun deleteReport() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getReportService().deleteReport(header, reportId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) = Unit
        })
    }

    companion object {
        private const val EXTRA_ID = "id"
        private const val EXTRA_MODEL = "model"
        private const val EXTRA_STATUS = "status"
        private const val EXTRA_SERIAL = "serial"
        private const val EXTRA_CREATED_AT = "created_at"
        private const val EXTRA_DESCRIPTION = "description"
        private const val EXTRA_REJECTION = "rejection"
        private const val EXTRA_FILE_COUNT = "file_count"

        fun putReport(intent: Intent, report: ReportResponse) {
            intent.putExtra(EXTRA_ID, report.id ?: -1L)
            intent.putExtra(EXTRA_MODEL, report.itemModel)
            intent.putExtra(EXTRA_STATUS, report.status)
            intent.putExtra(EXTRA_SERIAL, report.serialNumber)
            intent.putExtra(EXTRA_CREATED_AT, report.createdAt)
            intent.putExtra(EXTRA_DESCRIPTION, report.description)
            intent.putExtra(EXTRA_REJECTION, report.rejectionReason)
            intent.putExtra(EXTRA_FILE_COUNT, report.files?.size ?: 0)
        }
    }
}
