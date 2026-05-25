package com.cit.pawnscan.features.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.auth.LoginActivity
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.auth.api.UserProfileUpdateRequest
import com.cit.pawnscan.features.reports.api.MatchedReportResponse
import com.cit.pawnscan.features.reports.api.ReportResponse
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class UserPortalActivity : AppCompatActivity() {
    private enum class PortalTab { DASHBOARD, REPORTS, CREATE, MATCHES, PROFILE }

    private lateinit var scrollView: ScrollView
    private lateinit var statusMessage: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var totalReports: TextView
    private lateinit var totalMatches: TextView
    private lateinit var recentReportsList: LinearLayout
    private lateinit var reportsList: LinearLayout
    private lateinit var matchesList: LinearLayout
    private lateinit var createTitle: TextView
    private lateinit var serialInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var selectedFilesText: TextView
    private lateinit var submitReportButton: Button
    private lateinit var cancelEditButton: Button
    private lateinit var profileMeta: TextView
    private lateinit var profileFullName: EditText
    private lateinit var profilePhone: EditText

    private val sections = mutableMapOf<PortalTab, View>()
    private val navItems = mutableMapOf<PortalTab, TextView>()
    private val reports = mutableListOf<ReportResponse>()
    private val matches = mutableListOf<MatchedReportResponse>()
    private val selectedFiles = mutableListOf<Uri>()
    private var editingReport: ReportResponse? = null
    private var profile: UserProfileResponse? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedFiles.clear()
        selectedFiles.addAll(uris)
        updateSelectedFilesText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_portal)
        bindViews()
        bindActions()

        if (authHeader() == null) {
            navigateToLogin()
            return
        }

        userEmail.text = JwtStorageUtil.getUserEmail(this) ?: "Signed in"
        userName.text = "PawnScan User"
        showTab(PortalTab.DASHBOARD)
        loadPortalData()
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.portal_scroll)
        statusMessage = findViewById(R.id.portal_status)
        userName = findViewById(R.id.portal_user_name)
        userEmail = findViewById(R.id.portal_user_email)
        totalReports = findViewById(R.id.dashboard_total_reports)
        totalMatches = findViewById(R.id.dashboard_total_matches)
        recentReportsList = findViewById(R.id.dashboard_recent_reports)
        reportsList = findViewById(R.id.reports_list)
        matchesList = findViewById(R.id.matches_list)
        createTitle = findViewById(R.id.create_title)
        serialInput = findViewById(R.id.create_serial)
        modelInput = findViewById(R.id.create_model)
        descriptionInput = findViewById(R.id.create_description)
        selectedFilesText = findViewById(R.id.create_selected_files)
        submitReportButton = findViewById(R.id.create_submit)
        cancelEditButton = findViewById(R.id.create_cancel_edit)
        profileMeta = findViewById(R.id.profile_meta)
        profileFullName = findViewById(R.id.profile_full_name)
        profilePhone = findViewById(R.id.profile_phone)

        sections[PortalTab.DASHBOARD] = findViewById(R.id.section_dashboard)
        sections[PortalTab.REPORTS] = findViewById(R.id.section_reports)
        sections[PortalTab.CREATE] = findViewById(R.id.section_create)
        sections[PortalTab.MATCHES] = findViewById(R.id.section_matches)
        sections[PortalTab.PROFILE] = findViewById(R.id.section_profile)

        navItems[PortalTab.DASHBOARD] = findViewById(R.id.nav_home)
        navItems[PortalTab.REPORTS] = findViewById(R.id.nav_reports)
        navItems[PortalTab.CREATE] = findViewById(R.id.nav_create)
        navItems[PortalTab.MATCHES] = findViewById(R.id.nav_matches)
        navItems[PortalTab.PROFILE] = findViewById(R.id.nav_profile)
    }

    private fun bindActions() {
        findViewById<Button>(R.id.portal_logout).setOnClickListener {
            JwtStorageUtil.clearAll(this)
            navigateToLogin()
        }
        findViewById<Button>(R.id.dashboard_new_report).setOnClickListener {
            resetReportForm()
            showTab(PortalTab.CREATE)
        }
        findViewById<Button>(R.id.dashboard_view_reports).setOnClickListener { showTab(PortalTab.REPORTS) }
        findViewById<Button>(R.id.reports_refresh).setOnClickListener { loadReports() }
        findViewById<Button>(R.id.create_choose_files).setOnClickListener { filePicker.launch("*/*") }
        submitReportButton.setOnClickListener { submitReport() }
        cancelEditButton.setOnClickListener {
            resetReportForm()
            showTab(PortalTab.REPORTS)
        }
        findViewById<Button>(R.id.profile_save).setOnClickListener { saveProfile() }
        navItems.forEach { (tab, view) -> view.setOnClickListener { showTab(tab) } }
    }

    private fun loadPortalData() {
        loadProfile()
        loadReports()
        loadMatches()
    }

    private fun loadReports() {
        val header = authHeader() ?: return navigateToLogin()
        showStatus("Loading reports...", false)
        RetrofitClient.getReportService().getReports(header).enqueue(object : Callback<List<ReportResponse>> {
            override fun onResponse(call: Call<List<ReportResponse>>, response: Response<List<ReportResponse>>) {
                if (!response.isSuccessful) {
                    showStatus("Unable to load reports.", true)
                    return
                }
                reports.clear()
                reports.addAll((response.body() ?: emptyList()).sortedByDescending { it.createdAt ?: "" })
                renderReports()
                showStatus("Reports updated.", false)
            }

            override fun onFailure(call: Call<List<ReportResponse>>, t: Throwable) {
                showStatus("Could not reach the server for reports.", true)
            }
        })
    }

    private fun loadMatches() {
        val header = authHeader() ?: return navigateToLogin()
        RetrofitClient.getReportService().getMatchedReports(header).enqueue(object : Callback<List<MatchedReportResponse>> {
            override fun onResponse(call: Call<List<MatchedReportResponse>>, response: Response<List<MatchedReportResponse>>) {
                if (!response.isSuccessful) return
                matches.clear()
                matches.addAll((response.body() ?: emptyList()).sortedByDescending { it.matchedAt ?: "" })
                renderMatches()
            }

            override fun onFailure(call: Call<List<MatchedReportResponse>>, t: Throwable) {
                showStatus("Could not load matched reports.", true)
            }
        })
    }

    private fun loadProfile() {
        val header = authHeader() ?: return navigateToLogin()
        RetrofitClient.getAuthService().getProfile(header).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (!response.isSuccessful || response.body() == null) return
                profile = response.body()
                renderProfile()
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                showStatus("Could not load your profile.", true)
            }
        })
    }

    private fun renderReports() {
        totalReports.text = "${reports.size}\nReports"
        recentReportsList.removeAllViews()
        reportsList.removeAllViews()

        if (reports.isEmpty()) {
            recentReportsList.addView(emptyText(getString(R.string.portal_empty_reports)))
            reportsList.addView(emptyText(getString(R.string.portal_empty_reports)))
            return
        }

        reports.take(3).forEach { recentReportsList.addView(reportCard(it, false)) }
        reports.forEach { reportsList.addView(reportCard(it, true)) }
    }

    private fun renderMatches() {
        totalMatches.text = "${matches.size}\nMatches"
        matchesList.removeAllViews()
        if (matches.isEmpty()) {
            matchesList.addView(emptyText(getString(R.string.portal_empty_matches)))
            return
        }
        matches.forEach { match ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_matched_report_card, matchesList, false)
            view.findViewById<TextView>(R.id.match_model).text = match.itemModel ?: "Matched item"
            view.findViewById<TextView>(R.id.match_serial).text = "SN: ${match.serialNumber ?: "Unavailable"}"
            view.findViewById<TextView>(R.id.match_business).text = match.matchedByBusinessName ?: "Verified business"
            view.findViewById<TextView>(R.id.match_contact).text =
                listOfNotNull(match.matchedByBusinessEmail, match.matchedByBusinessPhone).joinToString(" | ")
                    .ifBlank { "No contact details provided" }
            view.findViewById<TextView>(R.id.match_address).text = match.matchedByBusinessAddress ?: "No address provided"
            view.findViewById<TextView>(R.id.match_date).text = "Matched: ${formatDate(match.matchedAt)}"
            matchesList.addView(view)
        }
    }

    private fun renderProfile() {
        val current = profile ?: return
        userName.text = current.fullName?.ifBlank { "PawnScan User" } ?: "PawnScan User"
        userEmail.text = current.email ?: JwtStorageUtil.getUserEmail(this) ?: "Signed in"
        profileFullName.setText(current.fullName.orEmpty())
        profilePhone.setText(current.phoneNumber ?: "+639")
        profileMeta.text = "Account: ${current.role ?: "USER"}\nEmail: ${current.email ?: "Unavailable"}\nMember since: ${formatDate(current.createdAt)}"
    }

    private fun reportCard(report: ReportResponse, includeActions: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_user_report_card, reportsList, false)
        view.findViewById<TextView>(R.id.report_model).text = report.itemModel ?: "Reported item"
        view.findViewById<TextView>(R.id.report_status).text = statusLabel(report.status)
        view.findViewById<TextView>(R.id.report_serial).text = "SN: ${report.serialNumber ?: "Unavailable"}"
        view.findViewById<TextView>(R.id.report_description).text = report.description ?: "No description provided"
        view.findViewById<TextView>(R.id.report_date).text = "Created: ${formatDate(report.createdAt)}"
        val actions = view.findViewById<LinearLayout>(R.id.report_actions)
        actions.visibility = if (includeActions) View.VISIBLE else View.GONE
        view.findViewById<Button>(R.id.report_edit).setOnClickListener { startEdit(report) }
        view.findViewById<Button>(R.id.report_delete).setOnClickListener { confirmDelete(report) }
        return view
    }

    private fun submitReport() {
        val serial = serialInput.text.toString().trim()
        val model = modelInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        if (serial.isBlank() || model.isBlank() || description.isBlank()) {
            showStatus("Serial number, item model, and description are required.", true)
            return
        }
        if (editingReport == null && selectedFiles.isEmpty()) {
            showStatus("Please attach at least one image or PDF as evidence.", true)
            return
        }

        val header = authHeader() ?: return navigateToLogin()
        val files = selectedFiles.mapNotNull { uri -> multipartFromUri(uri) }
        val textType = "text/plain".toMediaTypeOrNull()
        val call = editingReport?.id?.let { id ->
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

        submitReportButton.isEnabled = false
        submitReportButton.text = if (editingReport == null) "Submitting..." else "Saving..."
        call.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                submitReportButton.isEnabled = true
                if (!response.isSuccessful) {
                    submitReportButton.text = if (editingReport == null) getString(R.string.portal_submit_report) else getString(R.string.portal_update_report)
                    showStatus("Unable to save report. Check the details and try again.", true)
                    return
                }
                resetReportForm()
                loadReports()
                showTab(PortalTab.REPORTS)
                showStatus("Report saved successfully.", false)
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                submitReportButton.isEnabled = true
                submitReportButton.text = if (editingReport == null) getString(R.string.portal_submit_report) else getString(R.string.portal_update_report)
                showStatus("Could not reach the server while saving.", true)
            }
        })
    }

    private fun startEdit(report: ReportResponse) {
        editingReport = report
        createTitle.text = if (report.status == "REJECTED") "Resubmit Report" else "Edit Report"
        submitReportButton.text = getString(R.string.portal_update_report)
        cancelEditButton.visibility = View.VISIBLE
        serialInput.setText(report.serialNumber.orEmpty())
        modelInput.setText(report.itemModel.orEmpty())
        descriptionInput.setText(report.description.orEmpty())
        selectedFiles.clear()
        updateSelectedFilesText()
        showTab(PortalTab.CREATE)
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
        val header = authHeader() ?: return navigateToLogin()
        RetrofitClient.getReportService().deleteReport(header, id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    reports.removeAll { it.id == id }
                    renderReports()
                    showStatus("Report deleted.", false)
                } else {
                    showStatus("Unable to delete report.", true)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showStatus("Could not reach the server while deleting.", true)
            }
        })
    }

    private fun saveProfile() {
        val fullName = profileFullName.text.toString().trim()
        val phone = profilePhone.text.toString().trim()
        if (fullName.isBlank()) {
            showStatus("Full name is required.", true)
            return
        }
        if (!Regex("^\\+639\\d{9}$").matches(phone)) {
            showStatus("Use a valid Philippine mobile number, e.g. +639171234567.", true)
            return
        }
        val header = authHeader() ?: return navigateToLogin()
        RetrofitClient.getAuthService()
            .updateProfile(header, UserProfileUpdateRequest(fullName, phone))
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        profile = response.body()
                        renderProfile()
                        showStatus("Profile updated successfully.", false)
                    } else {
                        showStatus("Unable to save profile.", true)
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    showStatus("Could not reach the server while saving profile.", true)
                }
            })
    }

    private fun multipartFromUri(uri: Uri): MultipartBody.Part? {
        return try {
            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            if (!mime.startsWith("image/") && mime != "application/pdf") {
                showStatus("Only image and PDF evidence is allowed.", true)
                return null
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val name = displayName(uri)
            val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", name, body)
        } catch (e: Exception) {
            null
        }
    }

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return "evidence-${System.currentTimeMillis()}"
    }

    private fun resetReportForm() {
        editingReport = null
        createTitle.text = getString(R.string.portal_create_title)
        submitReportButton.text = getString(R.string.portal_submit_report)
        cancelEditButton.visibility = View.GONE
        serialInput.text.clear()
        modelInput.text.clear()
        descriptionInput.text.clear()
        selectedFiles.clear()
        updateSelectedFilesText()
    }

    private fun updateSelectedFilesText() {
        selectedFilesText.text = if (selectedFiles.isEmpty()) {
            if (editingReport == null) "No evidence selected" else "Existing evidence will remain unless you add more files."
        } else {
            selectedFiles.joinToString("\n") { displayName(it) }
        }
    }

    private fun showTab(tab: PortalTab) {
        sections.forEach { (sectionTab, view) -> view.visibility = if (sectionTab == tab) View.VISIBLE else View.GONE }
        navItems.forEach { (navTab, view) ->
            view.setTextColor(getColor(if (navTab == tab) R.color.brand_green else R.color.text_muted_gray))
        }
        scrollView.post { scrollView.smoothScrollTo(0, 0) }
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

    private fun showStatus(message: String, isError: Boolean) {
        statusMessage.text = message
        statusMessage.visibility = View.VISIBLE
        statusMessage.setTextColor(getColor(if (isError) R.color.text_red else R.color.brand_green))
    }

    private fun authHeader(): String? {
        val token = JwtStorageUtil.getToken(this) ?: return null
        return "Bearer $token"
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun statusLabel(status: String?): String {
        return when (status) {
            "PENDING" -> "Pending Review"
            "REJECTED" -> "Rejected"
            "APPROVED" -> "Approved"
            else -> "Approved"
        }
    }

    private fun formatDate(value: String?): String {
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
}
