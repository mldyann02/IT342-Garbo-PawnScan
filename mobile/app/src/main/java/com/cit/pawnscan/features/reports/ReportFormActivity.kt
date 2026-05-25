package com.cit.pawnscan.features.reports

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.reports.api.ReportResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportFormActivity : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var statusMessage: TextView
    private lateinit var serialInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var selectedFilesText: TextView
    private lateinit var submitButton: Button
    private val selectedFiles = mutableListOf<Uri>()
    private var reportId: Long? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedFiles.clear()
        selectedFiles.addAll(uris)
        updateSelectedFilesText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_form)

        if (PortalUi.requireAuth(this) == null) return

        bindViews()
        PortalUi.configureBottomNav(this, "new")
        readEditIntent()
        bindActions()
        updateSelectedFilesText()
    }

    private fun bindViews() {
        title = findViewById(R.id.report_form_title)
        statusMessage = findViewById(R.id.report_form_status)
        serialInput = findViewById(R.id.report_form_serial)
        modelInput = findViewById(R.id.report_form_model)
        descriptionInput = findViewById(R.id.report_form_description)
        selectedFilesText = findViewById(R.id.report_form_selected_files)
        submitButton = findViewById(R.id.report_form_submit)
    }

    private fun bindActions() {
        findViewById<Button>(R.id.report_form_choose_files).setOnClickListener { filePicker.launch("*/*") }
        findViewById<Button>(R.id.report_form_cancel).setOnClickListener { finish() }
        submitButton.setOnClickListener { submitReport() }
    }

    private fun readEditIntent() {
        if (!intent.hasExtra(EXTRA_REPORT_ID)) return
        reportId = intent.getLongExtra(EXTRA_REPORT_ID, -1L).takeIf { it > 0 }
        val status = intent.getStringExtra(EXTRA_STATUS).orEmpty()
        title.text = if (status == "REJECTED") "Resubmit Report" else "Edit Report"
        submitButton.text = getString(R.string.portal_update_report)
        serialInput.setText(intent.getStringExtra(EXTRA_SERIAL).orEmpty())
        modelInput.setText(intent.getStringExtra(EXTRA_MODEL).orEmpty())
        descriptionInput.setText(intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty())
    }

    private fun submitReport() {
        val serial = serialInput.text.toString().trim()
        val model = modelInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        if (serial.isBlank() || model.isBlank() || description.isBlank()) {
            PortalUi.showStatus(statusMessage, "Serial number, item model, and description are required.", true)
            return
        }
        if (reportId == null && selectedFiles.isEmpty()) {
            PortalUi.showStatus(statusMessage, "Please attach at least one image or PDF as evidence.", true)
            return
        }

        val header = PortalUi.requireAuth(this) ?: return
        val files = selectedFiles.mapNotNull { multipartFromUri(it) }
        val textType = "text/plain".toMediaTypeOrNull()
        val call = reportId?.let { id ->
            RetrofitClient.getReportService().updateReport(
                header,
                id,
                serial.toRequestBody(textType),
                model.toRequestBody(textType),
                description.toRequestBody(textType),
                files
            )
        } ?: RetrofitClient.getReportService().createReport(
            header,
            serial.toRequestBody(textType),
            model.toRequestBody(textType),
            description.toRequestBody(textType),
            files
        )

        submitButton.isEnabled = false
        submitButton.text = if (reportId == null) "Submitting..." else "Saving..."
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                submitButton.isEnabled = true
                if (response.isSuccessful) {
                    PortalUi.showStatus(statusMessage, "Report saved successfully.", false)
                    finish()
                    return
                }
                submitButton.text = if (reportId == null) getString(R.string.portal_submit_report) else getString(R.string.portal_update_report)
                PortalUi.showStatus(statusMessage, "Unable to save report. Check the details and try again.", true)
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                submitButton.isEnabled = true
                submitButton.text = if (reportId == null) getString(R.string.portal_submit_report) else getString(R.string.portal_update_report)
                PortalUi.showStatus(statusMessage, "Could not reach the server while saving.", true)
            }
        })
    }

    private fun multipartFromUri(uri: Uri): MultipartBody.Part? {
        return try {
            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            if (!mime.startsWith("image/") && mime != "application/pdf") {
                PortalUi.showStatus(statusMessage, "Only image and PDF evidence is allowed.", true)
                return null
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val name = PortalUi.displayName(this, uri)
            val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", name, body)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateSelectedFilesText() {
        selectedFilesText.text = if (selectedFiles.isEmpty()) {
            if (reportId == null) "No evidence selected" else "Existing evidence will remain unless you add more files."
        } else {
            selectedFiles.joinToString("\n") { PortalUi.displayName(this, it) }
        }
    }

    companion object {
        const val EXTRA_REPORT_ID = "report_id"
        const val EXTRA_SERIAL = "serial"
        const val EXTRA_MODEL = "model"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_STATUS = "status"
    }
}
