package com.cit.pawnscan.features.business

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
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
        findViewById<TextView>(R.id.business_match_detail_files).text =
            "Evidence attached: ${intent.getIntExtra(EXTRA_FILE_COUNT, 0)} file(s)"
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

        fun putMatch(intent: Intent, match: StolenMatchResponse) {
            intent.putExtra(EXTRA_SERIAL, match.searchedSerial)
            intent.putExtra(EXTRA_TIMESTAMP, match.timestamp)
            intent.putExtra(EXTRA_MODEL, match.itemModel)
            intent.putExtra(EXTRA_DESCRIPTION, match.description)
            intent.putExtra(EXTRA_OWNER, match.victimName)
            intent.putExtra(EXTRA_EMAIL, match.victimEmail)
            intent.putExtra(EXTRA_PHONE, match.victimPhoneNumber)
            intent.putExtra(EXTRA_FILE_COUNT, match.files?.size ?: 0)
        }
    }
}
