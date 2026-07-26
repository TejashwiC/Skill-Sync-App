package com.skillsync.app.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.R
import com.skillsync.app.data.model.Session

class CalendarSessionAdapter(
    private val sessions: List<Session>,
    private val currentUid: String,
    private val onJoinClick: (Session) -> Unit,
    private val onDeleteClick: (Session) -> Unit
) : RecyclerView.Adapter<CalendarSessionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCalSessionName)
        val tvDetails: TextView = view.findViewById(R.id.tvCalSessionDetails)
        val tvStatus: TextView = view.findViewById(R.id.tvCalSessionStatus)
        val btnJoin: Button = view.findViewById(R.id.btnCalJoin)
        val btnDelete: Button = view.findViewById(R.id.btnCalDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_session, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        val isOwner = session.hostId == currentUid

        holder.tvName.text = session.name

        val startMs = if (session.isScheduled && session.scheduledTime > 0) session.scheduledTime else session.startTime
        val endMs = if (session.endTime > 0) session.endTime else (startMs + (session.durationMins.coerceAtLeast(60L) * 60000L))
        val startStr = android.text.format.DateFormat.format("h:mm a", java.util.Date(startMs)).toString()
        val endStr = android.text.format.DateFormat.format("h:mm a", java.util.Date(endMs)).toString()

        holder.tvDetails.text = "Topic: ${session.skill} | Host: ${session.hostName}\n⏰ Time: $startStr – $endStr | Platform: ${session.platformLabel}"

        val now = System.currentTimeMillis()
        when {
            session.status == "live" && now in startMs..endMs -> {
                holder.tvStatus.text = "🔴 LIVE"
                holder.tvStatus.setTextColor(0xFFE53935.toInt())
                holder.btnJoin.text = if (isOwner) "🚀 Launch Session" else "▶ Enter Live Session"
            }
            session.status == "scheduled" || now < startMs -> {
                holder.tvStatus.text = "🟣 SCHEDULED"
                holder.tvStatus.setTextColor(0xFF7B1FA2.toInt())
                holder.btnJoin.text = if (isOwner) "🚀 Launch Session" else "▶ Join Session"
            }
            now >= endMs -> {
                holder.tvStatus.text = "⬛ ENDED"
                holder.tvStatus.setTextColor(0xFF757575.toInt())
                holder.btnJoin.isEnabled = false
                holder.btnJoin.text = "Session Ended"
            }
            else -> {
                holder.tvStatus.text = "🟢 UPCOMING"
                holder.tvStatus.setTextColor(0xFF388E3C.toInt())
                holder.btnJoin.text = "▶ Join Session"
            }
        }

        holder.btnJoin.setOnClickListener { onJoinClick(session) }

        // Delete button only visible for own sessions
        if (isOwner) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick(session) }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }
}
