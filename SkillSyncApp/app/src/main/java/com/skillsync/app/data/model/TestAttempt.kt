package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TestAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val creatorName: String = "",
    val skill: String = "",
    val difficulty: String = "",
    val score: Long = 0,
    val total: Long = 0,
    val correctAnswers: Long = 0,
    val wrongAnswers: Long = 0,
    val percentage: Long = 0,
    val timeTakenSec: Long = 0,
    val timeTakenStr: String = "",
    val earnedCredits: Long = 0,
    @ServerTimestamp val attemptedAt: Date? = null
)
