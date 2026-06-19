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
import coil.load

class ChatMessageAdapter(
    private var messages: List<ChatMessage> = emptyList(),
    var onMessagePin: ((ChatMessage) -> Unit)? = null
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
                binding.tvSentText.visibility = if (message.text.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                binding.tvSentTime.text = message.time.formatTime()
                
                binding.llAudioSent.visibility = if (message.audioUrl != null) android.view.View.VISIBLE else android.view.View.GONE
                binding.llPdfSent.visibility = if (message.pdfUrl != null) android.view.View.VISIBLE else android.view.View.GONE
                
                if (message.imageUrl != null) {
                    binding.ivImageSent.show()
                    binding.ivImageSent.load(message.imageUrl) {
                        crossfade(true)
                    }
                } else {
                    binding.ivImageSent.hide()
                }

                binding.tvSentStatus.text = when(message.status) {
                    "read" -> "✓✓"
                    "delivered" -> "✓"
                    else -> "✓"
                }
                if (message.status == "read") binding.tvSentStatus.setTextColor(android.graphics.Color.parseColor("#4FC3F7")) // Blue tick
                else binding.tvSentStatus.setTextColor(android.graphics.Color.parseColor("#E0F2F1"))

            } else {
                binding.layoutSent.hide()
                binding.layoutReceived.show()
                
                if (message.senderName.isNotEmpty()) {
                    binding.tvSenderName.text = message.senderName
                    binding.tvSenderName.show()
                } else {
                    binding.tvSenderName.hide()
                }

                binding.tvReceivedText.text = message.text
                binding.tvReceivedText.visibility = if (message.text.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                binding.tvReceivedTime.text = message.time.formatTime()

                binding.llAudioReceived.visibility = if (message.audioUrl != null) android.view.View.VISIBLE else android.view.View.GONE
                binding.llPdfReceived.visibility = if (message.pdfUrl != null) android.view.View.VISIBLE else android.view.View.GONE

                if (message.imageUrl != null) {
                    binding.ivImageReceived.show()
                    binding.ivImageReceived.load(message.imageUrl) {
                        crossfade(true)
                    }
                } else {
                    binding.ivImageReceived.hide()
                }
            }
            
            // Highlight or indicate if pinned
            if (message.isPinned) {
                if (isMyMessage) {
                    binding.tvSentText.text = "📌 ${message.text}"
                } else {
                    binding.tvReceivedText.text = "📌 ${message.text}"
                }
            }
            
            binding.root.setOnLongClickListener {
                onMessagePin?.invoke(message)
                true
            }
        }
    }
}
