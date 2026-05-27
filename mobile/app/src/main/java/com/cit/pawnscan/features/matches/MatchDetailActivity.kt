package com.cit.pawnscan.features.matches

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.reports.api.MatchedReportResponse
import com.cit.pawnscan.shared.ui.PortalUi

class MatchDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)

        findViewById<ImageButton>(R.id.match_detail_back).setOnClickListener { finish() }

        val title = intent.getStringExtra(EXTRA_MODEL).orEmpty().ifBlank { "Matched report" }
        val businessName = intent.getStringExtra(EXTRA_BUSINESS).orEmpty().ifBlank { "Verified business" }
        val contact = listOf(
            intent.getStringExtra(EXTRA_EMAIL),
            intent.getStringExtra(EXTRA_PHONE),
            intent.getStringExtra(EXTRA_ADDRESS)
        ).filterNot { it.isNullOrBlank() }.joinToString("\n").ifBlank { "No contact details provided" }

        findViewById<TextView>(R.id.match_detail_title).text = title
        findViewById<TextView>(R.id.match_detail_serial).text =
            "Serial number\n${intent.getStringExtra(EXTRA_SERIAL).orEmpty().ifBlank { "Unavailable" }}"
        findViewById<TextView>(R.id.match_detail_date).text =
            "Matched\n${PortalUi.formatDate(intent.getStringExtra(EXTRA_MATCHED_AT))}"
        findViewById<TextView>(R.id.match_detail_business).text = "Matched by\n$businessName"
        findViewById<TextView>(R.id.match_detail_contact).text = "Business contact\n$contact"
        findViewById<TextView>(R.id.match_detail_description).text =
            "Report details\n${intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty().ifBlank { "No description provided" }}"

        val evidenceUrls = intent.getStringArrayListExtra(EXTRA_FILE_URLS) ?: arrayListOf()
        val evidenceTypes = intent.getStringArrayListExtra(EXTRA_FILE_TYPES) ?: arrayListOf()
        val evidenceCount = if (evidenceUrls.isNotEmpty()) evidenceUrls.size
        else intent.getIntExtra(EXTRA_FILE_COUNT, 0)
        findViewById<TextView>(R.id.match_detail_files).text =
            "Evidence attached: $evidenceCount file(s)"
        renderEvidenceList(
            evidenceUrls,
            evidenceTypes,
            findViewById(R.id.match_detail_files_container),
            findViewById(R.id.match_detail_files_empty)
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
                    PortalUi.openEvidencePreview(this@MatchDetailActivity, normalized, types.getOrNull(index))
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
        private const val EXTRA_MODEL = "model"
        private const val EXTRA_SERIAL = "serial"
        private const val EXTRA_MATCHED_AT = "matched_at"
        private const val EXTRA_BUSINESS = "business"
        private const val EXTRA_EMAIL = "email"
        private const val EXTRA_PHONE = "phone"
        private const val EXTRA_ADDRESS = "address"
        private const val EXTRA_DESCRIPTION = "description"
        private const val EXTRA_FILE_COUNT = "file_count"
        private const val EXTRA_FILE_URLS = "file_urls"
        private const val EXTRA_FILE_TYPES = "file_types"

        fun putMatch(intent: Intent, match: MatchedReportResponse) {
            intent.putExtra(EXTRA_MODEL, match.itemModel)
            intent.putExtra(EXTRA_SERIAL, match.serialNumber)
            intent.putExtra(EXTRA_MATCHED_AT, match.matchedAt)
            intent.putExtra(EXTRA_BUSINESS, match.matchedByBusinessName)
            intent.putExtra(EXTRA_EMAIL, match.matchedByBusinessEmail)
            intent.putExtra(EXTRA_PHONE, match.matchedByBusinessPhone)
            intent.putExtra(EXTRA_ADDRESS, match.matchedByBusinessAddress)
            intent.putExtra(EXTRA_DESCRIPTION, match.description)
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
