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

    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_SESSIONS).document(sessionId).delete().await()
            Result.success(Unit)
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
                .whereIn("status", listOf("live", "scheduled"))
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid or expired session code."))
            }

            val doc = snapshot.documents.first()
            val session = doc.toObject(Session::class.java)!!
            val now = System.currentTimeMillis()
            val startTimeMs = if (session.startTime > 0) session.startTime else session.scheduledTime
            val durationMs = (if (session.durationMins > 0) session.durationMins else 60) * 60 * 1000L
            val endTimeMs = if (session.endTime > 0) session.endTime else (startTimeMs + durationMs)

            if (now >= endTimeMs) {
                db.collection(Constants.COLL_SESSIONS).document(doc.id).update("status", "ended").await()
                return Result.failure(Exception("Session time limit is over."))
            }

            if (session.status == "scheduled" && now >= startTimeMs) {
                db.collection(Constants.COLL_SESSIONS).document(doc.id).update("status", "live").await()
            }

            // Verify if host blocked this user
            val hostDoc = db.collection(Constants.COLL_USERS).document(session.hostId).get().await()
            val hostBlocked = hostDoc.get("blocked") as? List<*> ?: emptyList<Any>()
            if (hostBlocked.contains(userId)) {
                return Result.failure(Exception("Access denied by host."))
            }

            // Verify if user is following or followed by host
            val userDoc = db.collection(Constants.COLL_USERS).document(userId).get().await()
            val following = userDoc.get("following") as? List<*> ?: emptyList<Any>()
            val followers = userDoc.get("followers") as? List<*> ?: emptyList<Any>()
            val isRelated = session.hostId == userId || following.contains(session.hostId) || followers.contains(session.hostId)
            if (!isRelated) {
                return Result.failure(Exception("You must be following or followed by the host to join this session."))
            }

            // Add participant
            db.collection(Constants.COLL_SESSIONS).document(doc.id)
                .update("participants", FieldValue.arrayUnion(userId)).await()

            Result.success(session.copy(id = doc.id, status = "live"))
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
            .whereIn("status", listOf("live", "scheduled"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val docs = snapshot?.documents ?: emptyList()
                val visible = docs.mapNotNull { doc ->
                    val s = doc.toObject(Session::class.java) ?: return@mapNotNull null
                    val id = doc.id
                    val startTimeMs = if (s.startTime > 0) s.startTime else s.scheduledTime
                    val durationMs = (if (s.durationMins > 0) s.durationMins else 60) * 60 * 1000L
                    val endTimeMs = if (s.endTime > 0) s.endTime else (startTimeMs + durationMs)

                    if (now >= endTimeMs) {
                        if (s.status != "ended") {
                            db.collection(Constants.COLL_SESSIONS).document(id).update("status", "ended", "endTime", now)
                        }
                        return@mapNotNull null
                    }

                    val isLiveNow = s.status == "live" || (s.status == "scheduled" && now >= startTimeMs)
                    if (!isLiveNow) return@mapNotNull null

                    if (s.status == "scheduled" && now >= startTimeMs) {
                        db.collection(Constants.COLL_SESSIONS).document(id).update("status", "live")
                    }

                    s.copy(id = id, status = "live")
                }
                trySend(visible)
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

    fun observeMyCreatedEndedSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("hostId", uid)
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

    fun observeMyAttendedEndedSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("status", "ended")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) }
                    ?.filter { it.hostId != uid } ?: emptyList()
                val sortedSessions = sessions.sortedByDescending { it.startTime }
                trySend(sortedSessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyActiveSession(hostId: String): Flow<Session?> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("hostId", hostId)
            .whereIn("status", listOf("live", "scheduled"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val activeDoc = snapshot?.documents?.firstOrNull { doc ->
                    val s = doc.toObject(Session::class.java) ?: return@firstOrNull false
                    val startTimeMs = if (s.startTime > 0) s.startTime else s.scheduledTime
                    val durationMs = (if (s.durationMins > 0) s.durationMins else 60) * 60 * 1000L
                    val endTimeMs = if (s.endTime > 0) s.endTime else (startTimeMs + durationMs)
                    now >= startTimeMs && now < endTimeMs
                }
                val session = activeDoc?.toObject(Session::class.java)?.copy(id = activeDoc.id, status = "live")
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    suspend fun launchScheduledSession(sessionId: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_SESSIONS).document(sessionId).update(
                mapOf(
                    "status" to "live",
                    "startTime" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetches sessions for calendar: own sessions + mutual connections' sessions, today and future only
    fun observeMyCalendarSessions(uid: String, following: List<String>, followers: List<String>): Flow<List<Session>> = callbackFlow {
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Mutual connections = people I follow AND who follow me back
        val mutualConnections = following.filter { followers.contains(it) }
        // Visible host IDs: me + mutual connections
        val visibleHostIds = (listOf(uid) + mutualConnections).distinct()

        val listener = db.collection(Constants.COLL_SESSIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                val filtered = sessions.filter { session ->
                    val sessionStartMs = if (session.isScheduled && session.scheduledTime > 0) session.scheduledTime else session.startTime
                    val isNotEnded = session.status != "ended"
                    val isTodayOrFuture = sessionStartMs >= todayStart
                    val isVisible = visibleHostIds.contains(session.hostId)
                    isNotEnded && isTodayOrFuture && isVisible
                }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    // Only count active (live or scheduled) sessions hosted by this user — NOT history
    fun observeMyHostedSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val listener = db.collection(Constants.COLL_SESSIONS)
            .whereEqualTo("hostId", uid)
            .whereIn("status", listOf("live", "scheduled"))
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
