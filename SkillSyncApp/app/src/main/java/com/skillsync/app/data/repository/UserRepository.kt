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

    suspend fun uploadProfileImage(uid: String, imageUri: android.net.Uri): Result<String> {
        return try {
            val storageRef = FirebaseUtil.storage.reference.child("profile_images/$uid.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Auto update the user profile
            updateProfilePhoto(uid, downloadUrl)
            
            Result.success(downloadUrl)
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

    suspend fun updateAccountName(uid: String, newName: String): Result<Unit> {
        return try {
            val user = FirebaseUtil.currentUser
            val profileRequest = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user?.updateProfile(profileRequest)?.await()
            db.collection(Constants.COLL_USERS).document(uid).update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPass: String): Result<Unit> {
        return try {
            val user = FirebaseUtil.currentUser
            user?.updatePassword(newPass)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(uid: String): Result<Unit> {
        return try {
            val user = FirebaseUtil.currentUser

            // 1. Delete all tests created by user, associated questions, and attempts on user's tests
            try {
                val testsSnap = db.collection(Constants.COLL_TESTS).whereEqualTo("creatorId", uid).get().await()
                for (tDoc in testsSnap.documents) {
                    val qSnap = db.collection(Constants.COLL_QUESTIONS).whereEqualTo("testId", tDoc.id).get().await()
                    for (qDoc in qSnap.documents) { qDoc.reference.delete().await() }

                    val testAttSnap = db.collection(Constants.COLL_TEST_ATTEMPTS).whereEqualTo("testId", tDoc.id).get().await()
                    for (aDoc in testAttSnap.documents) { aDoc.reference.delete().await() }

                    tDoc.reference.delete().await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Delete test attempts by user
            try {
                val attemptsSnap = db.collection(Constants.COLL_TEST_ATTEMPTS).whereEqualTo("userId", uid).get().await()
                for (aDoc in attemptsSnap.documents) { aDoc.reference.delete().await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Delete chats involving user & all messages within
            try {
                val chatsSnap = db.collection(Constants.COLL_CHATS).get().await()
                for (cDoc in chatsSnap.documents) {
                    if (cDoc.id.contains(uid)) {
                        val msgsSnap = cDoc.reference.collection(Constants.COLL_MESSAGES).get().await()
                        for (mDoc in msgsSnap.documents) { mDoc.reference.delete().await() }
                        cDoc.reference.delete().await()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Delete sessions hosted by user
            try {
                val hostSessions = db.collection(Constants.COLL_SESSIONS).whereEqualTo("hostId", uid).get().await()
                for (sDoc in hostSessions.documents) { sDoc.reference.delete().await() }

                val hostUidSessions = db.collection(Constants.COLL_SESSIONS).whereEqualTo("hostUid", uid).get().await()
                for (sDoc in hostUidSessions.documents) { sDoc.reference.delete().await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 5. Delete PDFs uploaded by user
            try {
                val pdfSnap = db.collection(Constants.COLL_PDFS).whereEqualTo("uploadedBy", uid).get().await()
                for (pDoc in pdfSnap.documents) { pDoc.reference.delete().await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 6. Delete notifications for or from user
            try {
                val notifRec = db.collection("notifications").whereEqualTo("userId", uid).get().await()
                for (nDoc in notifRec.documents) { nDoc.reference.delete().await() }

                val notifSend = db.collection("notifications").whereEqualTo("senderId", uid).get().await()
                for (nDoc in notifSend.documents) { nDoc.reference.delete().await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 7. Remove user reference from other users' followers, following, and blocked lists
            try {
                val followersQuery = db.collection(Constants.COLL_USERS).whereArrayContains("followers", uid).get().await()
                for (doc in followersQuery.documents) { doc.reference.update("followers", FieldValue.arrayRemove(uid)).await() }

                val followingQuery = db.collection(Constants.COLL_USERS).whereArrayContains("following", uid).get().await()
                for (doc in followingQuery.documents) { doc.reference.update("following", FieldValue.arrayRemove(uid)).await() }

                val blockedQuery = db.collection(Constants.COLL_USERS).whereArrayContains("blocked", uid).get().await()
                for (doc in blockedQuery.documents) { doc.reference.update("blocked", FieldValue.arrayRemove(uid)).await() }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 8. Delete profile photo from Firebase Storage
            try {
                FirebaseUtil.storage.reference.child("profile_images/$uid.jpg").delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 9. Delete the user document from Firestore
            db.collection(Constants.COLL_USERS).document(uid).delete().await()

            // 10. Delete Firebase Auth user account
            user?.delete()?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
