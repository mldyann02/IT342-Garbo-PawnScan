package com.cit.pawnscan.features.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cit.pawnscan.R
import com.cit.pawnscan.features.reports.api.ReportResponse
import com.cit.pawnscan.shared.ui.PortalUi

class RecentReportsAdapter(
    private var reports: List<ReportResponse>,
    private val onItemClick: (ReportResponse) -> Unit
) : RecyclerView.Adapter<RecentReportsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val model: TextView = view.findViewById(R.id.report_model)
        val status: TextView = view.findViewById(R.id.report_status)
        val serial: TextView = view.findViewById(R.id.report_serial)
        val description: TextView = view.findViewById(R.id.report_description)
        val date: TextView = view.findViewById(R.id.report_date)
        val actions: LinearLayout = view.findViewById(R.id.report_actions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_report_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.model.text = report.itemModel ?: "Reported item"
        PortalUi.configureStatusBadge(holder.status, report.status)
        holder.serial.text = "SN: ${report.serialNumber ?: "Unavailable"}"
        holder.description.text = "Evidence: ${report.files?.size ?: 0} file(s)"
        holder.date.text = "Created: ${PortalUi.formatDate(report.createdAt)}"
        
        holder.actions.visibility = View.GONE
        
        holder.itemView.setOnClickListener { onItemClick(report) }
    }

    override fun getItemCount() = reports.size

    fun updateReports(newReports: List<ReportResponse>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
