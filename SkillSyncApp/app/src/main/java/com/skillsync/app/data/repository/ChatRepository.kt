package com.skillsync.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skillsync.app.data.model.ChatMessage
import com.skillsync.app.util.Constants
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class ChatRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore

    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    suspend fun sendMessage(chatId: String, text: String, senderUid: String, senderName: String, audioUrl: String? = null, pdfUrl: String? = null, imageUrl: String? = null): Result<Unit> {
        return try {
            val message = mutableMapOf(
                "text" to text,
                "sender" to senderUid,
                "senderName" to senderName,
                "time" to System.currentTimeMillis(),
                "status" to "sent" // sent, delivered, read
            )
            if (audioUrl != null) message["audioUrl"] = audioUrl
            if (pdfUrl != null) message["pdfUrl"] = pdfUrl
            if (imageUrl != null) message["imageUrl"] = imageUrl
            
            val docRef = db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
                .document()
            
            docRef.set(message).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadChatFile(chatId: String, uri: android.net.Uri, type: String): Result<String> = kotlin.coroutines.suspendCoroutine { continuation ->
        try {
            val extension = when(type) {
                "audio" -> ".3gp"
                "image" -> ".jpg"
                "pdf" -> ".pdf"
                else -> ""
            }
            val ref = FirebaseUtil.storage.reference.child("chats/$chatId/${System.currentTimeMillis()}_$type$extension")
            
            val uploadTask = if (uri.scheme == "file" && uri.path != null) {
                val file = java.io.File(uri.path!!)
                val bytes = file.readBytes()
                ref.putBytes(bytes)
            } else {
                ref.putFile(uri)
            }
            
            uploadTask.addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    continuation.resume(Result.success(downloadUri.toString()))
                }.addOnFailureListener { e ->
                    android.util.Log.e("ChatRepository", "downloadUrl failed", e)
                    continuation.resume(Result.failure(Exception("downloadUrl failed: ${e.message}")))
                }
            }.addOnFailureListener { e ->
                android.util.Log.e("ChatRepository", "uploadTask failed", e)
                continuation.resume(Result.failure(Exception("uploadTask failed: ${e.message}")))
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Upload exception", e)
            continuation.resume(Result.failure(e))
        }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUid: String) {
        try {
            val snapshot = db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
                .whereNotEqualTo("sender", currentUid)
                .whereEqualTo("status", "sent") // Simplification: change anything not 'read' to 'read'
                .get().await()
                
            db.runBatch { batch ->
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "status", "read")
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection(Constants.COLL_CHATS)
            .document(chatId)
            .collection(Constants.COLL_MESSAGES)
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            val messagesRef = db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
            val messages = messagesRef.get().await()
            db.runBatch { batch ->
                for (doc in messages) {
                    batch.delete(doc.reference)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createGroup(group: com.skillsync.app.data.model.Group, onComplete: (Boolean, String?) -> Unit) {
        val ref = db.collection(Constants.COLL_GROUPS).document()
        val g = group.copy(id = ref.id)
        ref.set(g).addOnSuccessListener {
            onComplete(true, ref.id)
        }.addOnFailureListener {
            onComplete(false, null)
        }
    }

    suspend fun getLatestMessage(chatId: String): ChatMessage? {
        return try {
            val snapshot = db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(ChatMessage::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun pinMessage(chatId: String, messageId: String, pinned: Boolean): Result<Unit> {
        return try {
            db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
                .document(messageId)
                .update("isPinned", pinned)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
