package com.cit.pawnscan.features.matches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.reports.api.MatchedReportResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchedReportsActivity : AppCompatActivity() {
    private lateinit var statusMessage: TextView
    private lateinit var matchesList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matched_reports)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.matches_status)
        matchesList = findViewById(R.id.matches_list)
        PortalUi.configureBottomNav(this, "matches")
        loadMatches()
    }

    private fun loadMatches() {
        val header = PortalUi.requireAuth(this) ?: return
        PortalUi.showStatus(statusMessage, "Checking business matches...", false)
        RetrofitClient.getReportService().getMatchedReports(header).enqueue(object : Callback<List<MatchedReportResponse>> {
            override fun onResponse(call: Call<List<MatchedReportResponse>>, response: Response<List<MatchedReportResponse>>) {
                if (!response.isSuccessful) {
                    PortalUi.showStatus(statusMessage, "Unable to load matched reports.", true)
                    return
                }
                renderMatches((response.body() ?: emptyList()).sortedByDescending { it.matchedAt ?: "" })
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<MatchedReportResponse>>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for matches.", true)
            }
        })
    }

    private fun renderMatches(matches: List<MatchedReportResponse>) {
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
                listOfNotNull(match.matchedByBusinessEmail, match.matchedByBusinessPhone)
                    .joinToString(" | ")
                    .ifBlank { "No contact details provided" }
            view.findViewById<TextView>(R.id.match_address).text = match.matchedByBusinessAddress ?: "No address provided"
            view.findViewById<TextView>(R.id.match_date).text = "Matched: ${PortalUi.formatDate(match.matchedAt)}"
            matchesList.addView(view)
        }
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
