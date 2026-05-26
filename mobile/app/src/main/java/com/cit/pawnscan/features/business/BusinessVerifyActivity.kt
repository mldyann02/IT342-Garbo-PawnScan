package com.cit.pawnscan.features.business

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.business.api.SearchLogResponse
import com.cit.pawnscan.features.business.api.VerifySearchResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusinessVerifyActivity : AppCompatActivity() {
    private lateinit var serialInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var statusMessage: TextView
    private lateinit var resultPanel: LinearLayout
    private lateinit var resultStatus: TextView
    private lateinit var resultSerial: TextView
    private lateinit var resultDetails: TextView
    private lateinit var recentList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_verify)

        if (BusinessPortalUi.requireBusiness(this) == null) return

        serialInput = findViewById(R.id.verify_serial_input)
        verifyButton = findViewById(R.id.verify_submit)
        statusMessage = findViewById(R.id.verify_status)
        resultPanel = findViewById(R.id.verify_result_panel)
        resultStatus = findViewById(R.id.verify_result_status)
        resultSerial = findViewById(R.id.verify_result_serial)
        resultDetails = findViewById(R.id.verify_result_details)
        recentList = findViewById(R.id.verify_recent_searches)

        BusinessPortalUi.configureBottomNav(this, "verify")
        verifyButton.isEnabled = false
        verifyButton.setOnClickListener { verifySerial() }
        BusinessPortalUi.requireVerifiedBusiness(this, statusMessage) {
            verifyButton.isEnabled = true
            loadRecentSearches()
        }
    }

    private fun verifySerial() {
        val serial = serialInput.text.toString().trim()
        if (serial.isBlank() || serial.length > 255 || !Regex("^[A-Za-z0-9][A-Za-z0-9 _#:/.-]*$").matches(serial)) {
            PortalUi.showStatus(statusMessage, "Enter a valid serial number.", true)
            return
        }

        val header = PortalUi.requireAuth(this) ?: return
        verifyButton.isEnabled = false
        verifyButton.text = "Checking..."
        resultPanel.visibility = View.GONE

        RetrofitClient.getVerificationService().verifySerial(header, serial)
            .enqueue(object : Callback<VerifySearchResponse> {
                override fun onResponse(call: Call<VerifySearchResponse>, response: Response<VerifySearchResponse>) {
                    verifyButton.isEnabled = true
                    verifyButton.text = "Verify Item"
                    val result = response.body()
                    if (!response.isSuccessful || result == null) {
                        PortalUi.showStatus(statusMessage, "Unable to verify that serial number.", true)
                        return
                    }
                    statusMessage.visibility = View.GONE
                    renderResult(result)
                    loadRecentSearches()
                }

                override fun onFailure(call: Call<VerifySearchResponse>, t: Throwable) {
                    verifyButton.isEnabled = true
                    verifyButton.text = "Verify Item"
                    PortalUi.showStatus(statusMessage, "Could not reach the server for verification.", true)
                }
            })
    }

    private fun renderResult(result: VerifySearchResponse) {
        val isStolen = result.status == "STOLEN"
        resultPanel.visibility = View.VISIBLE
        resultStatus.text = if (isStolen) "Stolen Match" else "Clean"
        resultStatus.setBackgroundResource(if (isStolen) R.drawable.badge_status_rejected else R.drawable.badge_status_approved)
        resultStatus.setTextColor(getColor(if (isStolen) R.color.text_red else R.color.brand_green))
        resultSerial.text = "Serial searched\n${result.serial ?: "Unavailable"}"

        resultDetails.text = if (isStolen) {
            val report = result.report
            if (report != null) {
                "Item model\n${report.itemModel ?: "Unspecified"}\n\nOwner\n${report.ownerName ?: report.victimName ?: "Not disclosed"}\n\nDescription\n${report.description ?: "No description provided"}"
            } else if (result.publicApiStolen == true) {
                "Public registry match\n${result.publicApiMatchTitle ?: result.publicApiSource ?: "External stolen item registry"}"
            } else {
                "A stolen match was found."
            }
        } else {
            "No approved stolen report matched this serial number."
        }
    }

    private fun loadRecentSearches() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getVerificationService().getSearchHistory(header, 0, 5)
            .enqueue(object : Callback<List<SearchLogResponse>> {
                override fun onResponse(call: Call<List<SearchLogResponse>>, response: Response<List<SearchLogResponse>>) {
                    if (response.isSuccessful) renderRecent(response.body().orEmpty())
                }

                override fun onFailure(call: Call<List<SearchLogResponse>>, t: Throwable) = Unit
            })
    }

    private fun renderRecent(searches: List<SearchLogResponse>) {
        recentList.removeAllViews()
        searches.forEach { search ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_business_search_card, recentList, false)
            view.findViewById<TextView>(R.id.search_serial).text = search.searchedSerial ?: "Unknown serial"
            view.findViewById<TextView>(R.id.search_model).text = search.itemModel ?: "Model unavailable"
            view.findViewById<TextView>(R.id.search_date).text = PortalUi.formatDate(search.timestamp)
            val badge = view.findViewById<TextView>(R.id.search_result)
            val isStolen = search.result == "STOLEN"
            badge.text = if (isStolen) "Stolen Match" else "Clean"
            badge.setBackgroundResource(if (isStolen) R.drawable.badge_status_rejected else R.drawable.badge_status_approved)
            badge.setTextColor(getColor(if (isStolen) R.color.text_red else R.color.brand_green))
            recentList.addView(view)
        }
    }
}
