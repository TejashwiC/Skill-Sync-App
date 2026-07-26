package com.skillsync.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skillsync.app.data.model.PdfNote
import com.skillsync.app.util.Constants
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PdfRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore
    private val storage: FirebaseStorage = FirebaseUtil.storage

    fun observeAllPdfs(): Flow<List<PdfNote>> = callbackFlow {
        val listener = db.collection(Constants.COLL_PDFS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val pdfs = snapshot?.documents?.mapNotNull { it.toObject(PdfNote::class.java) } ?: emptyList()
                trySend(pdfs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun uploadPdf(fileName: String, fileBytes: ByteArray, uploadedBy: String, uploadedByEmail: String, uploaderId: String): Result<Unit> {
        return try {
            // Encode the file bytes to a base64 string matching the web client
            val base64String = android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP)
            val dataURL = "data:application/pdf;base64,$base64String"

            // Save metadata and inline data directly to Firestore
            val pdfNote = mapOf(
                "fileName" to fileName,
                "fileURL" to dataURL,
                "uploadedBy" to uploadedBy,
                "uploadedByEmail" to uploadedByEmail,
                "uploaderId" to uploaderId,
                "uploadedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection(Constants.COLL_PDFS).add(pdfNote).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePdf(pdfId: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_PDFS).document(pdfId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
