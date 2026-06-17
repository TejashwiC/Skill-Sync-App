package com.skillsync.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.skillsync.app.data.model.User
import com.skillsync.app.util.Constants
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val snapshot = db.collection(Constants.COLL_USERS).document(uid).get().await()
            Result.success(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserProfile(uid: String): Flow<User?> = callbackFlow {
        val listener = db.collection(Constants.COLL_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(
        uid: String,
        name: String,
        email: String,
        mobile: String,
        teach: String,
        learn: String,
        language: String
    ): Result<Unit> {
        return try {
            val skillsList = teach.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            db.collection(Constants.COLL_USERS).document(uid).update(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "mobile" to mobile,
                    "teach" to teach,
                    "learn" to learn,
                    "language" to language,
                    "skills" to skillsList
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSetting(uid: String, fieldName: String, value: Boolean): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(uid).update(fieldName, value).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfilePhoto(uid: String, photoUrl: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(uid).update("photo", photoUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCredits(uid: String, amount: Long): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(uid)
                .update("credits", FieldValue.increment(amount)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(myUid: String, targetUid: String): Result<Unit> {
        return try {
            db.runBatch { batch ->
                val myRef = db.collection(Constants.COLL_USERS).document(myUid)
                val targetRef = db.collection(Constants.COLL_USERS).document(targetUid)
                batch.update(myRef, "following", FieldValue.arrayUnion(targetUid))
                batch.update(targetRef, "followers", FieldValue.arrayUnion(myUid))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(myUid: String, targetUid: String): Result<Unit> {
        return try {
            db.runBatch { batch ->
                val myRef = db.collection(Constants.COLL_USERS).document(myUid)
                val targetRef = db.collection(Constants.COLL_USERS).document(targetUid)
                batch.update(myRef, "following", FieldValue.arrayRemove(targetUid))
                batch.update(targetRef, "followers", FieldValue.arrayRemove(myUid))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(myUid: String, targetUid: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(myUid)
                .update("blocked", FieldValue.arrayUnion(targetUid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(myUid: String, targetUid: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_USERS).document(myUid)
                .update("blocked", FieldValue.arrayRemove(targetUid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection(Constants.COLL_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val usersList = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                trySend(usersList)
            }
        awaitClose { listener.remove() }
    }
}
