package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class Test(
    @DocumentId val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val title: String = "",
    val skill: String = "",
    val credits: Long = 0L,
    val createdAt: Long = 0L
)
