package com.cit.pawnscan.features.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cit.pawnscan.R
import com.cit.pawnscan.features.dashboard.api.NotificationResponse
import com.cit.pawnscan.shared.network.RetrofitClient
import com.cit.pawnscan.shared.ui.PortalUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnMarkAllRead: TextView
    private lateinit var btnClearAll: ImageButton

    private lateinit var adapter: NotificationAdapter
    private var authHeader: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Require authentication on screen load
        authHeader = PortalUi.requireAuth(this)
        if (authHeader == null) return

        initViews()
        setupRecyclerView()
        setupListeners()
        loadNotifications(showLoader = true)
    }

    private fun initViews() {
        swipeRefresh = findViewById(R.id.swipe_refresh)
        recyclerView = findViewById(R.id.notifications_list)
        emptyState = findViewById(R.id.notif_empty_state)
        progressBar = findViewById(R.id.notif_progress)
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read)
        btnClearAll = findViewById(R.id.btn_clear_all)

        // Custom SwipeRefresh layout look matching pawnscan brand green
        swipeRefresh.setColorSchemeColors(getColor(R.color.brand_green))
        swipeRefresh.setProgressBackgroundColorSchemeColor(getColor(R.color.bg_input))
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            handleNotificationClick(notification)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        swipeRefresh.setOnRefreshListener {
            loadNotifications(showLoader = false)
        }

        btnMarkAllRead.setOnClickListener {
            markAllNotificationsAsRead()
        }

        btnClearAll.setOnClickListener {
            clearAllNotifications()
        }
    }

    private fun loadNotifications(showLoader: Boolean) {
        val token = authHeader ?: return
        if (showLoader) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.GONE
        }

        RetrofitClient.getNotificationService().getNotifications(token)
            .enqueue(object : Callback<List<NotificationResponse>> {
                override fun onResponse(
                    call: Call<List<NotificationResponse>>,
                    response: Response<List<NotificationResponse>>
                ) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    if (response.isSuccessful) {
                        val notifications = response.body() ?: emptyList()
                        updateUiState(notifications)
                    } else {
                        Toast.makeText(
                            this@NotificationsActivity,
                            "Failed to load notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUiState(emptyList())
                    }
                }

                override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Server connection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUiState(emptyList())
                }
            })
    }

    private fun updateUiState(notifications: List<NotificationResponse>) {
        if (notifications.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            btnMarkAllRead.visibility = View.GONE
            btnClearAll.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            
            // Show mark all/clear actions only if we have notifications
            btnClearAll.visibility = View.VISIBLE
            
            // Show "mark all read" action if there is at least one unread notification
            val hasUnread = notifications.any { !it.read }
            btnMarkAllRead.visibility = if (hasUnread) View.VISIBLE else View.GONE

            adapter.updateItems(notifications)
        }
    }

    private fun handleNotificationClick(notification: NotificationResponse) {
        // If it's already read, we do not need to hit the read API
        if (notification.read) return

        val token = authHeader ?: return
        RetrofitClient.getNotificationService().markAsRead(token, notification.notifId)
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    if (response.isSuccessful) {
                        // Reload list to get updated styles & read state
                        loadNotifications(showLoader = false)
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    // Fail silently, just log
                }
            })
    }

    private fun markAllNotificationsAsRead() {
        val token = authHeader ?: return
        progressBar.visibility = View.VISIBLE

        RetrofitClient.getNotificationService().markAllAsRead(token)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        loadNotifications(showLoader = false)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@NotificationsActivity,
                            "Failed to mark all as read",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Server connection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun clearAllNotifications() {
        val token = authHeader ?: return
        progressBar.visibility = View.VISIBLE

        RetrofitClient.getNotificationService().clearNotifications(token)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        loadNotifications(showLoader = false)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@NotificationsActivity,
                            "Failed to clear notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Server connection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
