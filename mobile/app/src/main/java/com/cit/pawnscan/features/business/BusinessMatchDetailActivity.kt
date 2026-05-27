package com.cit.pawnscan.features.business

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.business.api.SearchLogResponse
import com.cit.pawnscan.features.business.api.StolenMatchResponse
import com.cit.pawnscan.shared.ui.PortalUi

class BusinessMatchDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_match_detail)

        findViewById<ImageButton>(R.id.business_match_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.business_match_detail_title).text =
            intent.getStringExtra(EXTRA_MODEL).orEmpty().ifBlank { "Stolen Match" }
        findViewById<TextView>(R.id.business_match_detail_serial).text =
            "Serial number\n${intent.getStringExtra(EXTRA_SERIAL).orEmpty().ifBlank { "Unavailable" }}"
        findViewById<TextView>(R.id.business_match_detail_date).text =
            "Matched\n${PortalUi.formatDate(intent.getStringExtra(EXTRA_TIMESTAMP))}"
        findViewById<TextView>(R.id.business_match_detail_owner).text =
            "Owner\n${intent.getStringExtra(EXTRA_OWNER).orEmpty().ifBlank { "Undisclosed" }}\n${intent.getStringExtra(EXTRA_EMAIL).orEmpty().ifBlank { "Email unavailable" }}\n${intent.getStringExtra(EXTRA_PHONE).orEmpty().ifBlank { "Phone unavailable" }}"
        findViewById<TextView>(R.id.business_match_detail_description).text =
            intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty().ifBlank { "No description provided." }

        val evidenceUrls = intent.getStringArrayListExtra(EXTRA_FILE_URLS) ?: arrayListOf()
        val evidenceTypes = intent.getStringArrayListExtra(EXTRA_FILE_TYPES) ?: arrayListOf()
        val evidenceCount = if (evidenceUrls.isNotEmpty()) evidenceUrls.size
        else intent.getIntExtra(EXTRA_FILE_COUNT, 0)
        findViewById<TextView>(R.id.business_match_detail_files).text =
            "Evidence attached: $evidenceCount file(s)"
        renderEvidenceList(
            evidenceUrls,
            evidenceTypes,
            findViewById(R.id.business_match_detail_files_container),
            findViewById(R.id.business_match_detail_files_empty)
        )
    }

    private fun renderEvidenceList(
        urls: List<String>,
        types: List<String>,
        container: LinearLayout,
        emptyView: TextView
    ) {
        container.removeAllViews()
        if (urls.isEmpty()) {
            emptyView.visibility = android.view.View.VISIBLE
            return
        }

        emptyView.visibility = android.view.View.GONE
        val spacing = (10 * resources.displayMetrics.density).toInt()
        val padding = (14 * resources.displayMetrics.density).toInt()
        urls.forEachIndexed { index, rawUrl ->
            val normalized = PortalUi.resolveEvidenceUrl(rawUrl) ?: return@forEachIndexed
            val label = buildEvidenceLabel(types.getOrNull(index), normalized, index)
            val entry = TextView(this).apply {
                text = label
                setBackgroundResource(R.drawable.bg_button_glass_outline)
                setTextColor(getColor(R.color.text_white))
                textSize = 14f
                setPadding(padding, padding, padding, padding)
                setOnClickListener {
                    PortalUi.openEvidencePreview(this@BusinessMatchDetailActivity, normalized, types.getOrNull(index))
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = spacing
            container.addView(entry, params)
        }
    }

    private fun buildEvidenceLabel(fileType: String?, url: String, index: Int): String {
        val normalizedType = fileType?.uppercase() ?: ""
        val isPdf = normalizedType == "PDF" || url.endsWith(".pdf", ignoreCase = true)
        val typeLabel = if (isPdf) "PDF" else "Image"
        return "Evidence ${index + 1} - $typeLabel"
    }

    companion object {
        private const val EXTRA_SERIAL = "serial"
        private const val EXTRA_TIMESTAMP = "timestamp"
        private const val EXTRA_MODEL = "model"
        private const val EXTRA_DESCRIPTION = "description"
        private const val EXTRA_OWNER = "owner"
        private const val EXTRA_EMAIL = "email"
        private const val EXTRA_PHONE = "phone"
        private const val EXTRA_FILE_COUNT = "file_count"
        private const val EXTRA_FILE_URLS = "file_urls"
        private const val EXTRA_FILE_TYPES = "file_types"

        fun openFromSearch(activity: Activity, search: SearchLogResponse, matches: List<StolenMatchResponse>) {
            if (search.result != "STOLEN") return
            val match = matches.firstOrNull {
                (search.matchedReportId != null && it.matchedReportId == search.matchedReportId) ||
                    (!search.searchedSerial.isNullOrBlank() && it.searchedSerial.equals(search.searchedSerial, ignoreCase = true))
            } ?: StolenMatchResponse(
                searchedSerial = search.searchedSerial,
                timestamp = search.timestamp,
                matchedReportId = search.matchedReportId,
                itemModel = search.itemModel,
                description = "Open the Stolen Matches tab for the full owner and evidence details.",
                dateReported = null,
                victimName = null,
                victimEmail = null,
                victimPhoneNumber = null,
                files = emptyList()
            )
            val intent = Intent(activity, BusinessMatchDetailActivity::class.java)
            putMatch(intent, match)
            activity.startActivity(intent)
        }

        fun putMatch(intent: Intent, match: StolenMatchResponse) {
            intent.putExtra(EXTRA_SERIAL, match.searchedSerial)
            intent.putExtra(EXTRA_TIMESTAMP, match.timestamp)
            intent.putExtra(EXTRA_MODEL, match.itemModel)
            intent.putExtra(EXTRA_DESCRIPTION, match.description)
            intent.putExtra(EXTRA_OWNER, match.victimName)
            intent.putExtra(EXTRA_EMAIL, match.victimEmail)
            intent.putExtra(EXTRA_PHONE, match.victimPhoneNumber)
            val files = match.files?.mapNotNull { file ->
                file.fileUrl?.let { url -> url to (file.fileType ?: "") }
            } ?: emptyList()
            intent.putExtra(EXTRA_FILE_COUNT, files.size)
            intent.putStringArrayListExtra(
                EXTRA_FILE_URLS,
                ArrayList(files.map { it.first })
            )
            intent.putStringArrayListExtra(
                EXTRA_FILE_TYPES,
                ArrayList(files.map { it.second })
            )
        }
    }
}
