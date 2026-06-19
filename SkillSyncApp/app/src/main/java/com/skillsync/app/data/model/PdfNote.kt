package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class PdfNote(
    @DocumentId val id: String = "",
    val fileName: String = "",
    val fileURL: String = "",
    val uploadedBy: String = ""
)
