package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class Session(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val name: String = "",
    val skill: String = "",
    val platform: String = "",
    val platformLabel: String = "",
    val meetingLink: String = "",
    val code: String = "",
    val status: String = "live", // "live" or "ended"
    val startTime: Long = 0L,
    val participants: List<String> = emptyList(),
    val feedback: List<SessionFeedback> = emptyList(),
    val ratings: List<SessionRating> = emptyList()
)

data class SessionFeedback(
    val userId: String = "",
    val name: String = "",
    val text: String = "",
    val time: Long = 0L
)

data class SessionRating(
    val userId: String = "",
    val name: String = "",
    val stars: Long = 0L,
    val time: Long = 0L
)
