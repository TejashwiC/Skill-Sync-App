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
                if (user.photo.startsWith("data:image")) {
                    try {
                        val base64String = user.photo.substringAfter("base64,")
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.ivUserPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        binding.ivUserPhoto.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(user.name))
                    }
                } else {
                    binding.ivUserPhoto.load(user.photo) {
                        placeholder(android.R.drawable.sym_def_app_icon)
                        error(android.R.drawable.sym_def_app_icon)
                    }
                }
            } else {
                binding.ivUserPhoto.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(user.name))
            }

            // Repurpose follow button as "Open" trigger, style it
            binding.btnFollow.text = "Open"
            binding.btnViewProfile.visibility = android.view.View.GONE

            binding.btnFollow.setOnClickListener { onClick(item) }
            binding.root.setOnClickListener { onClick(item) }
            
            binding.root.setOnLongClickListener {
                val prefs = binding.root.context.getSharedPreferences("ChatPrefs", android.content.Context.MODE_PRIVATE)
                val pinnedSet = prefs.getStringSet("pinned_chats", mutableSetOf()) ?: mutableSetOf()
                val uid = item.otherUser.uid
                
                if (pinnedSet.contains(uid)) {
                    pinnedSet.remove(uid)
                    android.widget.Toast.makeText(binding.root.context, "Chat unpinned", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    pinnedSet.add(uid)
                    android.widget.Toast.makeText(binding.root.context, "Chat pinned", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                prefs.edit().putStringSet("pinned_chats", pinnedSet).apply()
                // notify adapter or fragment to refresh list
                // For a simple implementation, just re-submit the sorted list
                (binding.root.context as? android.app.Activity)?.recreate() // Quick refresh, or we can use a callback
                true
            }
            
            // Show pin icon if pinned
            val prefs = binding.root.context.getSharedPreferences("ChatPrefs", android.content.Context.MODE_PRIVATE)
            val pinnedSet = prefs.getStringSet("pinned_chats", mutableSetOf()) ?: mutableSetOf()
            if (pinnedSet.contains(user.uid)) {
                binding.tvUserName.text = "📌 ${user.name}"
            }
        }
    }
}
