package com.cit.pawnscan.features.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cit.pawnscan.R
import com.cit.pawnscan.features.dashboard.api.NotificationResponse
import com.cit.pawnscan.shared.ui.PortalUi

class NotificationAdapter(
    private var items: List<NotificationResponse>,
    private val onItemClick: (NotificationResponse) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view
        val icon: ImageView = view.findViewById(R.id.notif_icon)
        val unreadDot: View = view.findViewById(R.id.notif_unread_dot)
        val title: TextView = view.findViewById(R.id.notif_title)
        val message: TextView = view.findViewById(R.id.notif_message)
        val time: TextView = view.findViewById(R.id.notif_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        holder.title.text = item.title ?: "PawnScan Notification"
        holder.message.text = item.message ?: ""
        holder.time.text = PortalUi.formatDate(item.createdAt)

        // Dynamic icon color: Red alert if it's a stolen item match, green otherwise
        val isAlert = item.title?.contains("match", ignoreCase = true) == true || 
                      item.message?.contains("stolen", ignoreCase = true) == true
        
        val context = holder.itemView.context
        if (isAlert) {
            holder.icon.setImageResource(R.drawable.ic_logo_shield)
            holder.icon.imageTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.status_stolen))
        } else {
            holder.icon.setImageResource(R.drawable.ic_notification)
            holder.icon.imageTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.brand_green))
        }

        // Apply unread/read styles
        if (!item.read) {
            holder.unreadDot.visibility = View.VISIBLE
            holder.title.alpha = 1.0f
            holder.message.alpha = 1.0f
            holder.container.background = context.getDrawable(R.drawable.bg_glass_panel)
        } else {
            holder.unreadDot.visibility = View.GONE
            holder.title.alpha = 0.6f
            holder.message.alpha = 0.6f
            holder.container.background = context.getDrawable(R.drawable.bg_glass_panel_soft)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<NotificationResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
