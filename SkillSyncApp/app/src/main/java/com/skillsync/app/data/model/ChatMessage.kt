package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId val id: String = "",
    val text: String = "",
    val sender: String = "",
    val time: Long = 0L
)
