package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skillsync.app.R
import com.skillsync.app.databinding.ItemUserBinding
import com.skillsync.app.ui.chat.ChatViewModel
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.toTimeAgo

class InboxAdapter(
    private var items: List<ChatViewModel.InboxItem> = emptyList(),
    private val onClick: (ChatViewModel.InboxItem) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    fun submitList(newList: List<ChatViewModel.InboxItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        // We reuse the clean ItemUserBinding structure, styling last message inside tvUserEmail
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InboxViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatViewModel.InboxItem) {
            val user = item.otherUser
            val message = item.lastMessage
            
            binding.tvUserName.text = user.name
            
            val prefix = if (message.sender == FirebaseUtil.currentUid) "You: " else ""
            val time = message.time.toTimeAgo()
            binding.tvUserEmail.text = "$prefix${message.text} · $time"

            if (user.photo.isNotEmpty() && user.photo.length > 10) {
                binding.ivUserPhoto.load(user.photo) {
                    placeholder(android.R.drawable.sym_def_app_icon)
                    error(android.R.drawable.sym_def_app_icon)
                }
            } else {
                binding.ivUserPhoto.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            // Repurpose follow button as "Open" trigger, style it
            binding.btnFollow.text = "Open"
            binding.btnViewProfile.visibility = android.view.View.GONE

            binding.btnFollow.setOnClickListener { onClick(item) }
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
