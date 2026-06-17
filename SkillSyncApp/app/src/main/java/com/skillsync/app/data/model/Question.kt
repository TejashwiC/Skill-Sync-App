package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class Question(
    @DocumentId val id: String = "",
    val testId: String = "",
    val creatorId: String = "",
    val question: String = "",
    val option1: String = "",
    val option2: String = "",
    val option3: String = "",
    val option4: String = "",
    val correctAnswer: String = ""
)
