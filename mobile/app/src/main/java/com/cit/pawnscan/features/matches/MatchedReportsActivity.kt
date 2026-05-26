package com.cit.pawnscan.features.matches

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var searchInput: TextView
    private val matches = mutableListOf<MatchedReportResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matched_reports)

        if (PortalUi.requireAuth(this) == null) return

        statusMessage = findViewById(R.id.matches_status)
        matchesList = findViewById(R.id.matches_list)
        searchInput = findViewById(R.id.matches_search)
        PortalUi.configureBottomNav(this, "matches")
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderMatches()
            override fun afterTextChanged(s: Editable?) = Unit
        })
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
                matches.clear()
                matches.addAll((response.body() ?: emptyList()).sortedByDescending { it.matchedAt ?: "" })
                renderMatches()
                statusMessage.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<MatchedReportResponse>>, t: Throwable) {
                PortalUi.showStatus(statusMessage, "Could not reach the server for matches.", true)
            }
        })
    }

    private fun renderMatches() {
        matchesList.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()
        val visibleMatches = matches.filter { match ->
            val haystack = listOf(
                match.itemModel,
                match.serialNumber,
                match.description,
                match.matchedByBusinessName,
                match.matchedByBusinessEmail,
                match.matchedByBusinessPhone,
                match.matchedByBusinessAddress
            ).joinToString(" ").lowercase()
            query.isBlank() || haystack.contains(query)
        }
        if (visibleMatches.isEmpty()) {
            matchesList.addView(emptyText(getString(R.string.portal_empty_matches)))
            return
        }
        visibleMatches.forEach { match ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_matched_report_card, matchesList, false)
            view.findViewById<TextView>(R.id.match_model).text = match.itemModel ?: "Matched item"
            view.findViewById<TextView>(R.id.match_serial).text = "SN: ${match.serialNumber ?: "Unavailable"}"
            view.findViewById<TextView>(R.id.match_business).text = "Matched by ${match.matchedByBusinessName ?: "Verified business"}"
            view.findViewById<TextView>(R.id.match_contact).text =
                listOfNotNull(match.matchedByBusinessEmail, match.matchedByBusinessPhone)
                    .joinToString(" | ")
                    .ifBlank { "No contact details provided" }
            view.findViewById<TextView>(R.id.match_address).text = match.matchedByBusinessAddress ?: "Address unavailable"
            view.findViewById<TextView>(R.id.match_date).text = "Matched: ${PortalUi.formatDate(match.matchedAt)}"
            view.setOnClickListener { openDetail(match) }
            matchesList.addView(view)
        }
    }

    private fun openDetail(match: MatchedReportResponse) {
        val intent = Intent(this, MatchDetailActivity::class.java)
        MatchDetailActivity.putMatch(intent, match)
        startActivity(intent)
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
