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

    suspend fun uploadPdf(fileName: String, fileUri: Uri, uploadedBy: String, onProgress: (Double) -> Unit = {}): Result<Unit> {
        return try {
            val fileRef = storage.reference.child("pdfs/${System.currentTimeMillis()}_$fileName")
            val uploadTask = fileRef.putFile(fileUri)
            
            // Listen for progress changes
            uploadTask.addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                onProgress(progress)
            }
            
            // Wait for upload completion
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            // Save metadata to Firestore
            val pdfNote = mapOf(
                "fileName" to fileName,
                "fileURL" to downloadUrl,
                "uploadedBy" to uploadedBy
            )
            db.collection(Constants.COLL_PDFS).add(pdfNote).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
