package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.Session
import com.skillsync.app.databinding.ItemSessionBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.formatDate
import com.skillsync.app.util.formatTime
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import java.util.Locale

class SessionAdapter(
    private var sessions: List<Session> = emptyList(),
    private val onJoinClick: (Session) -> Unit,
    private val onEndClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    fun submitList(newSessions: List<Session>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    inner class SessionViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: Session) {
            binding.tvSessionName.text = session.name
            binding.tvSessionHost.text = "Host: ${session.hostName}"
            binding.tvSessionSkill.text = "Skill: ${session.skill}"
            binding.tvSessionPlatform.text = "Platform: ${session.platformLabel}"

            val currentUid = FirebaseUtil.currentUid
            val isHost = session.hostId == currentUid

            if (session.status == "live") {
                binding.tvSessionStatus.show()
                binding.tvSessionStatus.text = "LIVE"
                binding.tvSessionStatus.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                
                binding.tvSessionTime.text = "Started: ${session.startTime.formatTime()} · Running"
                
                if (isHost) {
                    binding.layoutSecretCode.show()
                    binding.tvSecretCode.text = session.code
                    binding.btnEndSession.show()
                    binding.btnJoinSession.text = "Rejoin Meeting"
                    binding.btnJoinSession.show()
                } else {
                    binding.layoutSecretCode.hide()
                    binding.btnEndSession.hide()
                    binding.btnJoinSession.text = "Join Meeting"
                    binding.btnJoinSession.show()
                }
                binding.tvSessionRating.hide()
            } else {
                binding.tvSessionStatus.show()
                binding.tvSessionStatus.text = "ENDED"
                binding.tvSessionStatus.setBackgroundColor(binding.root.context.getColor(android.R.color.darker_gray))
                
                binding.tvSessionTime.text = "Ended: ${session.startTime.formatDate()} · ${session.startTime.formatTime()}"
                
                binding.layoutSecretCode.hide()
                binding.btnEndSession.hide()
                binding.btnJoinSession.hide()

                if (session.ratings.isNotEmpty()) {
                    val avg = session.ratings.map { it.stars }.average()
                    binding.tvSessionRating.text = String.format(Locale.getDefault(), "Rating: ⭐ %.1f/5 (%d reviews)", avg, session.ratings.size)
                    binding.tvSessionRating.show()
                } else {
                    binding.tvSessionRating.text = "No ratings yet"
                    binding.tvSessionRating.show()
                }
            }

            binding.btnJoinSession.setOnClickListener { onJoinClick(session) }
            binding.btnEndSession.setOnClickListener { onEndClick(session) }
        }
    }
}
