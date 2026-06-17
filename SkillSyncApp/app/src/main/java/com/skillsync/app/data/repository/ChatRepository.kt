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

class ChatRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore

    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    suspend fun sendMessage(chatId: String, text: String, senderUid: String): Result<Unit> {
        return try {
            val message = mapOf(
                "text" to text,
                "sender" to senderUid,
                "time" to System.currentTimeMillis()
            )
            db.collection(Constants.COLL_CHATS)
                .document(chatId)
                .collection(Constants.COLL_MESSAGES)
                .add(message)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
                    batch.delete(doc.ref)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
}
