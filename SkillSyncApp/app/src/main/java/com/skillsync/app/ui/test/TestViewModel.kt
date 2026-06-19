package com.skillsync.app.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.Question
import com.skillsync.app.data.model.Test
import com.skillsync.app.data.model.TestAttempt
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.TestRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TestViewModel : ViewModel() {

    private val testRepository = TestRepository()
    private val userRepository = UserRepository()

    private val _myTests = MutableLiveData<List<Test>>()
    val myTests: LiveData<List<Test>> = _myTests

    private val _myQuestions = MutableLiveData<List<Question>>()
    val myQuestions: LiveData<List<Question>> = _myQuestions

    private val _myAttempts = MutableLiveData<List<TestAttempt>>()
    val myAttempts: LiveData<List<TestAttempt>> = _myAttempts

    private val _availableTests = MutableLiveData<List<Test>>()
    val availableTests: LiveData<List<Test>> = _availableTests

    private val _leaderboard = MutableLiveData<List<LeaderboardRow>>()
    val leaderboard: LiveData<List<LeaderboardRow>> = _leaderboard

    private val _actionResult = MutableLiveData<Result<Unit>?>()
    val actionResult: LiveData<Result<Unit>?> = _actionResult

    init {
        loadTestData()
    }

    private fun loadTestData() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // Observe my tests
        viewModelScope.launch {
            testRepository.observeMyTests(uid).collectLatest { list ->
                _myTests.postValue(list)
            }
        }

        // Observe my questions
        viewModelScope.launch {
            testRepository.observeMyQuestions(uid).collectLatest { list ->
                _myQuestions.postValue(list)
            }
        }

        // Observe my attempts
        viewModelScope.launch {
            testRepository.observeMyAttempts(uid).collectLatest { list ->
                _myAttempts.postValue(list)
            }
        }
    }

    fun createTest(title: String, skill: String, credits: Long) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return
        val creatorName = FirebaseUtil.currentUser?.displayName ?: "Expert"

        viewModelScope.launch {
            val result = testRepository.createTest(title, skill, credits, uid, creatorName)
            if (result.isSuccess) {
                _actionResult.postValue(Result.success(Unit))
            } else {
                _actionResult.postValue(Result.failure(result.exceptionOrNull() ?: Exception("Unknown error")))
            }
        }
    }

    fun deleteTest(testId: String) {
        viewModelScope.launch {
            val result = testRepository.deleteTest(testId)
            _actionResult.postValue(result)
        }
    }

    fun addQuestion(testId: String, question: String, option1: String, option2: String, option3: String, option4: String, correctAnswer: String) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val result = testRepository.addQuestion(testId, uid, question, option1, option2, option3, option4, correctAnswer)
            _actionResult.postValue(result)
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            val result = testRepository.deleteQuestion(questionId)
            _actionResult.postValue(result)
        }
    }

    fun loadAvailableTests() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            // Find mutual connections
            val myProfile = userRepository.getUserProfile(uid).getOrNull()
            val following = myProfile?.following ?: emptyList()
            val followers = myProfile?.followers ?: emptyList()
            val mutualUids = following.filter { followers.contains(it) }

            val tests = testRepository.getAvailableTests(mutualUids)
            _availableTests.postValue(tests)
        }
    }

    fun submitAttempt(test: Test, score: Long, total: Long) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // 10 credits earned per correct answer
        val earnedCredits = score * 10
        val percentage = if (total > 0L) ((score.toDouble() / total.toDouble()) * 100).toLong() else 0L

        val attempt = TestAttempt(
            userId = uid,
            testId = test.id,
            testTitle = test.title,
            score = score,
            total = total,
            percentage = percentage,
            earnedCredits = earnedCredits
        )

        viewModelScope.launch {
            val result = testRepository.submitAttempt(attempt)
            _actionResult.postValue(result)
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            val allAttempts = testRepository.getAllAttempts()
            val userStats = mutableMapOf<String, LeaderboardRow>()
            
            for (attempt in allAttempts) {
                val stats = userStats.getOrPut(attempt.userId) {
                    LeaderboardRow(attempt.userId, "Loading...", 0L)
                }
                userStats[attempt.userId] = stats.copy(
                    earnedCredits = stats.earnedCredits + attempt.earnedCredits
                )
            }

            // Fetch user names
            val rows = userStats.values.map { row ->
                val name = userRepository.getUserProfile(row.userId).getOrNull()?.name ?: "User"
                row.copy(userName = name)
            }.sortedByDescending { it.earnedCredits }

            _leaderboard.postValue(rows)
        }
    }

    fun resetActionResult() {
        _actionResult.value = null
    }

    data class LeaderboardRow(
        val userId: String,
        val userName: String,
        val earnedCredits: Long
    )
}
