package com.skillsync.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.skillsync.app.data.model.Session
import com.skillsync.app.data.model.SessionFeedback
import com.skillsync.app.data.model.SessionRating
import com.skillsync.app.util.Constants
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SessionRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore

    suspend fun startSession(session: Session): Result<String> {
        return try {
            // Check if there's already a live session hosted by this user
            val existing = db.collection(Constants.COLL_SESSIONS)
                .whereEqualTo("hostId", session.hostId)
                .whereEqualTo("status", "live")
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("You already have an active live session! End it first."))
            }

            val docRef = db.collection(Constants.COLL_SESSIONS).add(session).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endSession(sessionId: String, durationMins: Long): Result<Unit> {
        return try {
            db.collection(Constants.COLL_SESSIONS).document(sessionId).update(
                mapOf(
                    "status" to "ended",
                    "endTime" to System.currentTimeMillis(),
                    "durationMins" to durationMins
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinSession(code: String, userId: String): Result<Session> {
        return try {
            val snapshot = db.collection(Constants.COLL_SESSIONS)
                .whereEqualTo("code", code)
                .whereEqualTo("status", "live")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid or expired session code."))
            }

            val doc = snapshot.documents.first()
            val session = doc.toObject(Session::class.java)!!

            // Verify if host blocked this user
            val hostDoc = db.collection(Constants.COLL_USERS).document(session.hostId).get().await()
            val hostBlocked = hostDoc.get("blocked") as? List<*> ?: emptyList<Any>()
            if (hostBlocked.contains(userId)) {
                return Result.failure(Exception("Access denied by host."))
            }

            // Add participant
            db.collection(Constants.COLL_SESSIONS).document(doc.id)
                .update("participants", FieldValue.arrayUnion(userId)).await()

            Result.success(session.copy(id = doc.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitFeedback(sessionId: String, feedback: SessionFeedback): Result<Unit> {
        return try {
            db.collection(Constants.COLL_SESSIONS).document(sessionId)
                .update("feedback", FieldValue.arrayUnion(feedback)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRating(sessionId: String, rating: SessionRating): Result<Unit> {
        return try {
            val sessionDoc = db.collection(Constants.COLL_SESSIONS).document(sessionId).get().await()
            val ratings = sessionDoc.toObject(Session::class.java)?.ratings ?: emptyList()
            if (ratings.any { it.userId == rating.userId }) {
                return Result.failure(Exception("You have already rated this session."))
            }
            db.collection(Constants.COLL_SESSIONS).document(sessionId)
                .update("ratings", FieldValue.arrayUnion(rating)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeLiveSessions(): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("status", "live")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeSessionHistory(): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("status", "ended")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                val sortedSessions = sessions.sortedByDescending { it.startTime }
                trySend(sortedSessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyActiveSession(hostId: String): Flow<Session?> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("hostId", hostId)
            .whereEqualTo("status", "live")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val session = snapshot?.documents?.firstOrNull()?.toObject(Session::class.java)
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyCalendarSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                // Filter locally since Firestore OR queries can be complex across arrays
                val mySessions = sessions.filter { it.hostId == uid || it.participants.contains(uid) }
                trySend(mySessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyHostedSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("hostId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }
}
