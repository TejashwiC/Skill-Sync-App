package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId val messageId: String = "",
    val text: String = "",
    val sender: String = "",
    val senderName: String = "",
    val time: Long = 0L,
    val status: String = "sent", // sent, delivered, read
    val audioUrl: String? = null,
    val pdfUrl: String? = null,
    val imageUrl: String? = null,
    val isPinned: Boolean = false
)
