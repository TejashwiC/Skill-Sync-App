package com.skillsync.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class TestAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val score: Long = 0,
    val total: Long = 0,
    val percentage: Long = 0,
    val earnedCredits: Long = 0,
    @ServerTimestamp val attemptedAt: Timestamp? = null
)
