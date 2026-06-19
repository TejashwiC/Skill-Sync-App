package com.skillsync.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skillsync.app.data.model.Question
import com.skillsync.app.data.model.Test
import com.skillsync.app.data.model.TestAttempt
import com.skillsync.app.util.Constants
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TestRepository {
    private val db: FirebaseFirestore = FirebaseUtil.firestore

    suspend fun createTest(title: String, skill: String, credits: Long, creatorId: String, creatorName: String): Result<String> {
        return try {
            val testMap = mapOf(
                "title" to title,
                "skill" to skill,
                "credits" to credits,
                "creatorId" to creatorId,
                "creatorName" to creatorName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            val docRef = db.collection(Constants.COLL_TESTS).add(testMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTest(testId: String): Result<Unit> {
        return try {
            // Delete all questions belonging to this test
            val questionsSnap = db.collection(Constants.COLL_QUESTIONS)
                .whereEqualTo("testId", testId)
                .get()
                .await()

            db.runBatch { batch ->
                for (doc in questionsSnap.documents) {
                    batch.delete(doc.reference)
                }
                batch.delete(db.collection(Constants.COLL_TESTS).document(testId))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addQuestion(testId: String, creatorId: String, question: String, option1: String, option2: String, option3: String, option4: String, correctAnswer: String): Result<Unit> {
        return try {
            val questionMap = mapOf(
                "testId" to testId,
                "creatorId" to creatorId,
                "question" to question,
                "option1" to option1,
                "option2" to option2,
                "option3" to option3,
                "option4" to option4,
                "correctAnswer" to correctAnswer,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection(Constants.COLL_QUESTIONS).add(questionMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuestion(questionId: String): Result<Unit> {
        return try {
            db.collection(Constants.COLL_QUESTIONS).document(questionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeMyTests(creatorId: String): Flow<List<Test>> = callbackFlow {
        val listener = db.collection(Constants.COLL_TESTS)
            .whereEqualTo("creatorId", creatorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tests = snapshot?.documents?.mapNotNull { it.toObject(Test::class.java) } ?: emptyList()
                trySend(tests)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyQuestions(creatorId: String): Flow<List<Question>> = callbackFlow {
        val listener = db.collection(Constants.COLL_QUESTIONS)
            .whereEqualTo("creatorId", creatorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val questions = snapshot?.documents?.mapNotNull { it.toObject(Question::class.java) } ?: emptyList()
                trySend(questions)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getQuestionsForTest(testId: String): Result<List<Question>> {
        return try {
            val snapshot = db.collection(Constants.COLL_QUESTIONS)
                .whereEqualTo("testId", testId)
                .get()
                .await()
            val questions = snapshot.documents.mapNotNull { it.toObject(Question::class.java) }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableTests(mutualUids: List<String>): List<Test> {
        if (mutualUids.isEmpty()) return emptyList()
        return try {
            // Firestore limit is 10 items in "in" queries. Chop list into sublists of size 10.
            val chunks = mutualUids.chunked(10)
            val allTests = mutableListOf<Test>()
            for (chunk in chunks) {
                val snapshot = db.collection(Constants.COLL_TESTS)
                    .whereIn("creatorId", chunk)
                    .get()
                    .await()
                allTests.addAll(snapshot.documents.mapNotNull { it.toObject(Test::class.java) })
            }
            allTests
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTestDetails(testId: String): Test? {
        return try {
            val snapshot = db.collection(Constants.COLL_TESTS).document(testId).get().await()
            snapshot.toObject(Test::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitAttempt(attempt: TestAttempt): Result<Unit> {
        return try {
            db.runBatch { batch ->
                // Add test attempt record
                val attemptRef = db.collection(Constants.COLL_TEST_ATTEMPTS).document()
                batch.set(attemptRef, attempt)

                // Update user credits and test count
                val userRef = db.collection(Constants.COLL_USERS).document(attempt.userId)
                batch.update(userRef, "credits", FieldValue.increment(attempt.earnedCredits))
                batch.update(userRef, "tests", FieldValue.increment(1))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeMyAttempts(userId: String): Flow<List<TestAttempt>> = callbackFlow {
        val listener = db.collection(Constants.COLL_TEST_ATTEMPTS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val attempts = snapshot?.documents?.mapNotNull { it.toObject(TestAttempt::class.java) } ?: emptyList()
                val sortedAttempts = attempts.sortedByDescending { it.attemptedAt }
                trySend(sortedAttempts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllAttempts(): List<TestAttempt> {
        return try {
            val snapshot = db.collection(Constants.COLL_TEST_ATTEMPTS).get().await()
            snapshot.documents.mapNotNull { it.toObject(TestAttempt::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
