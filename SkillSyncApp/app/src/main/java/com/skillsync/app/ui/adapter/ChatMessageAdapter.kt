package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.ChatMessage
import com.skillsync.app.databinding.ItemChatMessageBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.formatTime
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class ChatMessageAdapter(
    private var messages: List<ChatMessage> = emptyList()
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            val isMyMessage = message.sender == FirebaseUtil.currentUid
            
            if (isMyMessage) {
                binding.layoutSent.show()
                binding.layoutReceived.hide()
                binding.tvSentText.text = message.text
                binding.tvSentTime.text = message.time.formatTime()
            } else {
                binding.layoutSent.hide()
                binding.layoutReceived.show()
                binding.tvReceivedText.text = message.text
                binding.tvReceivedTime.text = message.time.formatTime()
            }
        }
    }
}
