package com.cit.pawnscan.features.business

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import com.cit.pawnscan.features.business.api.SearchLogResponse
import com.cit.pawnscan.features.business.api.StolenMatchResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusinessHistoryActivity : AppCompatActivity() {
    private lateinit var tabSearches: TextView
    private lateinit var tabMatches: TextView
    private lateinit var searchInput: EditText
    private lateinit var list: LinearLayout
    private lateinit var statusMessage: TextView
    private val searches = mutableListOf<SearchLogResponse>()
    private val matches = mutableListOf<StolenMatchResponse>()
    private var activeTab = TAB_SEARCHES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_history)

        if (BusinessPortalUi.requireBusiness(this) == null) return

        activeTab = intent.getStringExtra(EXTRA_TAB) ?: TAB_SEARCHES
        tabSearches = findViewById(R.id.business_tab_searches)
        tabMatches = findViewById(R.id.business_tab_matches)
        searchInput = findViewById(R.id.business_history_search)
        list = findViewById(R.id.business_history_list)
        statusMessage = findViewById(R.id.business_history_status)

        BusinessPortalUi.configureBottomNav(this, if (activeTab == TAB_MATCHES) "matches" else "history")
        bindTabs()
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = render()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        loadData()
    }

    private fun bindTabs() {
        tabSearches.setOnClickListener {
            activeTab = TAB_SEARCHES
            BusinessPortalUi.configureBottomNav(this, "history")
            updateTabs()
            render()
        }
        tabMatches.setOnClickListener {
            activeTab = TAB_MATCHES
            BusinessPortalUi.configureBottomNav(this, "matches")
            updateTabs()
            render()
        }
        updateTabs()
    }

    private fun updateTabs() {
        val searchesActive = activeTab == TAB_SEARCHES
        tabSearches.setBackgroundResource(if (searchesActive) R.drawable.bg_status_tab_active else R.drawable.bg_status_tab_inactive)
        tabSearches.setTextColor(getColor(if (searchesActive) R.color.brand_green else R.color.text_light_gray))
        tabMatches.setBackgroundResource(if (!searchesActive) R.drawable.bg_status_tab_active else R.drawable.bg_status_tab_inactive)
        tabMatches.setTextColor(getColor(if (!searchesActive) R.color.brand_green else R.color.text_light_gray))
    }

    private fun loadData() {
        val header = PortalUi.requireAuth(this) ?: return
        RetrofitClient.getVerificationService().getSearchHistory(header, 0, 500)
            .enqueue(object : Callback<List<SearchLogResponse>> {
                override fun onResponse(call: Call<List<SearchLogResponse>>, response: Response<List<SearchLogResponse>>) {
                    if (response.isSuccessful) {
                        searches.clear()
                        searches.addAll(response.body().orEmpty())
                        render()
                    }
                }

                override fun onFailure(call: Call<List<SearchLogResponse>>, t: Throwable) {
                    PortalUi.showStatus(statusMessage, "Could not load search history.", true)
                }
            })
        RetrofitClient.getVerificationService().getStolenMatches(header, 0, 500)
            .enqueue(object : Callback<List<StolenMatchResponse>> {
                override fun onResponse(call: Call<List<StolenMatchResponse>>, response: Response<List<StolenMatchResponse>>) {
                    if (response.isSuccessful) {
                        matches.clear()
                        matches.addAll(response.body().orEmpty())
                        render()
                    }
                }

                override fun onFailure(call: Call<List<StolenMatchResponse>>, t: Throwable) {
                    PortalUi.showStatus(statusMessage, "Could not load stolen matches.", true)
                }
            })
    }

    private fun render() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val query = searchInput.text.toString().trim().lowercase()
        if (activeTab == TAB_SEARCHES) {
            val visible = searches.filter {
                listOf(it.searchedSerial, it.itemModel, it.result).joinToString(" ").lowercase().contains(query)
            }
            if (visible.isEmpty()) {
                list.addView(emptyText("No searches found."))
                return
            }
            visible.forEach { search ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_business_search_card, list, false)
                view.findViewById<TextView>(R.id.search_serial).text = search.searchedSerial ?: "Unknown serial"
                view.findViewById<TextView>(R.id.search_model).text = search.itemModel ?: "Model unavailable"
                view.findViewById<TextView>(R.id.search_date).text = PortalUi.formatDate(search.timestamp)
                configureBadge(view.findViewById(R.id.search_result), search.result)
                list.addView(view)
            }
        } else {
            val visible = matches.filter {
                listOf(it.searchedSerial, it.itemModel, it.victimName, it.description).joinToString(" ").lowercase().contains(query)
            }
            if (visible.isEmpty()) {
                list.addView(emptyText("No stolen matches found."))
                return
            }
            visible.forEach { match ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_business_match_card, list, false)
                view.findViewById<TextView>(R.id.business_match_serial).text = match.searchedSerial ?: "Unknown serial"
                view.findViewById<TextView>(R.id.business_match_model).text = match.itemModel ?: "Model unavailable"
                view.findViewById<TextView>(R.id.business_match_owner).text = match.victimName ?: "Owner undisclosed"
                view.findViewById<TextView>(R.id.business_match_date).text = "Matched ${PortalUi.formatDate(match.timestamp)}"
                view.setOnClickListener { openMatchDetail(match) }
                list.addView(view)
            }
        }
    }

    private fun configureBadge(view: TextView, result: String?) {
        val stolen = result == "STOLEN"
        view.text = if (stolen) "Stolen Match" else "Clean"
        view.setBackgroundResource(if (stolen) R.drawable.badge_status_rejected else R.drawable.badge_status_approved)
        view.setTextColor(getColor(if (stolen) R.color.text_red else R.color.brand_green))
    }

    private fun openMatchDetail(match: StolenMatchResponse) {
        val intent = Intent(this, BusinessMatchDetailActivity::class.java)
        BusinessMatchDetailActivity.putMatch(intent, match)
        startActivity(intent)
    }

    private fun emptyText(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(getColor(R.color.text_muted_gray))
            textSize = 14f
            setPadding(18, 24, 18, 24)
            setBackgroundResource(R.drawable.bg_glass_panel_soft)
        }
    }

    companion object {
        const val EXTRA_TAB = "tab"
        const val TAB_SEARCHES = "searches"
        const val TAB_MATCHES = "matches"
    }
}
