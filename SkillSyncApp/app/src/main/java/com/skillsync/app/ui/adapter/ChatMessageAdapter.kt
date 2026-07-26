package com.skillsync.app.ui.adapter

import android.graphics.Color
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
import android.content.Intent
import android.net.Uri

class ChatMessageAdapter(
    private var messages: List<ChatMessage> = emptyList(),
    var onMessagePin: ((ChatMessage) -> Unit)? = null,
    var onMessageDelete: ((ChatMessage) -> Unit)? = null,
    // Called when selection state changes: message selected or null when deselected
    var onSelectionChanged: ((ChatMessage?) -> Unit)? = null
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    // Currently selected message (null = none selected)
    private var selectedMessageId: String? = null

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val prev = selectedMessageId
        selectedMessageId = null
        // Refresh only the deselected item
        val idx = messages.indexOfFirst { it.messageId == prev }
        if (idx >= 0) notifyItemChanged(idx)
        onSelectionChanged?.invoke(null)
    }

    fun getSelectedMessage(): ChatMessage? = messages.find { it.messageId == selectedMessageId }

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
            val isSelected = message.messageId == selectedMessageId

            // Highlight background when selected
            binding.root.setBackgroundColor(
                if (isSelected) Color.parseColor("#FFE0B2") else Color.TRANSPARENT
            )

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
                    if (message.imageUrl.startsWith("data:image")) {
                        try {
                            val base64String = message.imageUrl.substringAfter("base64,")
                            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.ivImageSent.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            binding.ivImageSent.hide()
                        }
                    } else {
                        binding.ivImageSent.load(message.imageUrl) { crossfade(true) }
                    }
                } else {
                    binding.ivImageSent.hide()
                }

                binding.tvSentStatus.text = when (message.status) {
                    "read" -> "✓✓"
                    "delivered" -> "✓"
                    else -> "✓"
                }
                if (message.status == "read") binding.tvSentStatus.setTextColor(Color.parseColor("#4FC3F7"))
                else binding.tvSentStatus.setTextColor(Color.parseColor("#E0F2F1"))

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
                    if (message.imageUrl.startsWith("data:image")) {
                        try {
                            val base64String = message.imageUrl.substringAfter("base64,")
                            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.ivImageReceived.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            binding.ivImageReceived.hide()
                        }
                    } else {
                        binding.ivImageReceived.load(message.imageUrl) { crossfade(true) }
                    }
                } else {
                    binding.ivImageReceived.hide()
                }
            }

            // Pinned prefix
            if (message.isPinned) {
                if (isMyMessage) binding.tvSentText.text = "📌 ${message.text}"
                else binding.tvReceivedText.text = "📌 ${message.text}"
            }

            val openUrl = { url: String ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    itemView.context.startActivity(intent)
                } catch (e: Exception) { /* ignore */ }
            }

            binding.ivImageSent.setOnClickListener { message.imageUrl?.let { openUrl(it) } }
            binding.ivImageReceived.setOnClickListener { message.imageUrl?.let { openUrl(it) } }
            binding.llPdfSent.setOnClickListener { message.pdfUrl?.let { openUrl(it) } }
            binding.llPdfReceived.setOnClickListener { message.pdfUrl?.let { openUrl(it) } }

            // Single tap: if something is already selected, deselect
            binding.root.setOnClickListener {
                if (selectedMessageId != null) {
                    clearSelection()
                }
            }

            // Long press: select this message (only own messages can be deleted, but show selection for all)
            binding.root.setOnLongClickListener {
                val prev = selectedMessageId
                // Deselect previous
                if (prev != null && prev != message.messageId) {
                    val prevIdx = messages.indexOfFirst { it.messageId == prev }
                    selectedMessageId = null
                    if (prevIdx >= 0) notifyItemChanged(prevIdx)
                }
                // Toggle selection on this message
                if (selectedMessageId == message.messageId) {
                    selectedMessageId = null
                    notifyItemChanged(adapterPosition)
                    onSelectionChanged?.invoke(null)
                } else {
                    selectedMessageId = message.messageId
                    notifyItemChanged(adapterPosition)
                    onSelectionChanged?.invoke(message)
                }
                true
            }
        }
    }
}
