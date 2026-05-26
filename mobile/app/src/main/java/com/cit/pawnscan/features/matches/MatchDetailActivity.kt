package com.cit.pawnscan.features.matches

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
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
        findViewById<TextView>(R.id.match_detail_business).text = businessName
        findViewById<TextView>(R.id.match_detail_contact).text = contact
        findViewById<TextView>(R.id.match_detail_description).text =
            "Report details\n${intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty().ifBlank { "No description provided" }}"
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

        fun putMatch(intent: Intent, match: MatchedReportResponse) {
            intent.putExtra(EXTRA_MODEL, match.itemModel)
            intent.putExtra(EXTRA_SERIAL, match.serialNumber)
            intent.putExtra(EXTRA_MATCHED_AT, match.matchedAt)
            intent.putExtra(EXTRA_BUSINESS, match.matchedByBusinessName)
            intent.putExtra(EXTRA_EMAIL, match.matchedByBusinessEmail)
            intent.putExtra(EXTRA_PHONE, match.matchedByBusinessPhone)
            intent.putExtra(EXTRA_ADDRESS, match.matchedByBusinessAddress)
            intent.putExtra(EXTRA_DESCRIPTION, match.description)
        }
    }
}
