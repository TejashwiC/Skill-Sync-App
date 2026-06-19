package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val iconUrl: String? = null,
    val adminId: String = "",
    val members: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val createdAt: Long = 0L
)
