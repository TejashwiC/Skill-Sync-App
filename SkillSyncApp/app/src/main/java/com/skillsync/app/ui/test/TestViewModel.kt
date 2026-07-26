package com.skillsync.app.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.Question
import com.skillsync.app.data.model.Test
import com.skillsync.app.data.model.TestAttempt
import com.skillsync.app.data.repository.TestRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.QuestionBankItem
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

    private val _createdTestsWithAttendees = MutableLiveData<List<CreatedTestWithAttendees>>()
    val createdTestsWithAttendees: LiveData<List<CreatedTestWithAttendees>> = _createdTestsWithAttendees

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

        // Observe my tests & attendees
        viewModelScope.launch {
            testRepository.observeMyTests(uid).collectLatest { list ->
                _myTests.postValue(list)
                loadAttendeesForCreatedTests(list)
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

    private fun loadAttendeesForCreatedTests(tests: List<Test>) {
        viewModelScope.launch {
            val resultList = mutableListOf<CreatedTestWithAttendees>()
            for (t in tests) {
                val qCount = testRepository.getQuestionsCountForTest(t.id)
                val rawAttendees = testRepository.getAttemptsForTest(t.id)
                val attendees = rawAttendees.map { att ->
                    if (att.userName.isNotEmpty()) att
                    else {
                        val name = userRepository.getUserProfile(att.userId).getOrNull()?.name ?: "User"
                        att.copy(userName = name)
                    }
                }
                resultList.add(CreatedTestWithAttendees(test = t, questionCount = qCount, attendees = attendees))
            }
            _createdTestsWithAttendees.postValue(resultList)
        }
    }

    fun publishPreparedTest(skill: String, difficulty: String, questions: List<QuestionBankItem>) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val userProfile = userRepository.getUserProfile(uid).getOrNull()
            val creatorName = userProfile?.name ?: FirebaseUtil.currentUser?.displayName ?: "Creator"
            val result = testRepository.publishPreparedTest(skill, difficulty, questions, uid, creatorName)
            if (result.isSuccess) {
                _actionResult.postValue(Result.success(Unit))
            } else {
                _actionResult.postValue(Result.failure(result.exceptionOrNull() ?: Exception("Failed to publish test")))
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
            // Find connections (followers & following)
            val myProfile = userRepository.getUserProfile(uid).getOrNull()
            val following = myProfile?.following ?: emptyList()
            val followers = myProfile?.followers ?: emptyList()
            val connections = (following + followers).distinct().filter { it != uid }

            if (connections.isEmpty()) {
                _availableTests.postValue(emptyList())
                return@launch
            }

            val tests = testRepository.getAvailableTests(connections)
            val myAttempts = testRepository.getMyAttemptsOnce(uid)
            val attemptedIds = myAttempts.map { it.testId }.toSet()

            // Filter out tests created by self OR already attempted OR with 0 questions!
            val filtered = mutableListOf<Test>()
            for (t in tests) {
                if (t.creatorId != uid && !attemptedIds.contains(t.id)) {
                    val qCount = testRepository.getQuestionsCountForTest(t.id)
                    if (qCount > 0) {
                        filtered.add(t.copy(questionCount = qCount.toLong()))
                    }
                }
            }
            _availableTests.postValue(filtered)
        }
    }

    fun submitAttemptDetailed(
        test: Test,
        score: Long,
        total: Long,
        correctAnswers: Long,
        wrongAnswers: Long,
        timeTakenSec: Long,
        timeTakenStr: String
    ) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        val earnedCredits = score * 10
        val percentage = if (total > 0L) ((score.toDouble() / total.toDouble()) * 100).toLong() else 0L

        viewModelScope.launch {
            val userProfile = userRepository.getUserProfile(uid).getOrNull()
            val userName = userProfile?.name ?: FirebaseUtil.currentUser?.displayName ?: "User"

            val attempt = TestAttempt(
                userId = uid,
                userName = userName,
                testId = test.id,
                testTitle = test.title,
                creatorName = test.creatorName.ifEmpty { "Creator" },
                skill = test.skill,
                difficulty = test.difficulty,
                score = score,
                total = total,
                correctAnswers = correctAnswers,
                wrongAnswers = wrongAnswers,
                percentage = percentage,
                timeTakenSec = timeTakenSec,
                timeTakenStr = timeTakenStr,
                earnedCredits = earnedCredits
            )

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

    data class CreatedTestWithAttendees(
        val test: Test,
        val questionCount: Int,
        val attendees: List<TestAttempt>
    )

    data class LeaderboardRow(
        val userId: String,
        val userName: String,
        val earnedCredits: Long
    )
}
