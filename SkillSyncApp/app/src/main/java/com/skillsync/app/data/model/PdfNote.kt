package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PdfNote(
    @DocumentId val id: String = "",
    val fileName: String = "",
    val fileURL: String = "",
    val uploadedBy: String = "",
    val uploadedByEmail: String = "",
    val uploaderId: String = "",
    @ServerTimestamp val uploadedAt: Date? = null
)
