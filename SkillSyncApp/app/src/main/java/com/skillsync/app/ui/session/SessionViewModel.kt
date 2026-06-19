package com.skillsync.app.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.Session
import com.skillsync.app.data.model.SessionFeedback
import com.skillsync.app.data.model.SessionRating
import com.skillsync.app.data.repository.SessionRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {

    private val sessionRepository = SessionRepository()

    private val _liveSessions = MutableLiveData<List<Session>>()
    val liveSessions: LiveData<List<Session>> = _liveSessions

    private val _endedSessions = MutableLiveData<List<Session>>()
    val endedSessions: LiveData<List<Session>> = _endedSessions

    private val _myActiveSession = MutableLiveData<Session?>()
    val myActiveSession: LiveData<Session?> = _myActiveSession

    private val _startResult = MutableLiveData<Result<String>?>()
    val startResult: LiveData<Result<String>?> = _startResult

    private val _joinResult = MutableLiveData<Result<Session>?>()
    val joinResult: LiveData<Result<Session>?> = _joinResult

    private val _actionResult = MutableLiveData<Result<Unit>?>()
    val actionResult: LiveData<Result<Unit>?> = _actionResult

    init {
        loadSessions()
    }

    private fun loadSessions() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // Observe live sessions
        viewModelScope.launch {
            sessionRepository.observeLiveSessions().collectLatest { list ->
                _liveSessions.postValue(list)
            }
        }

        // Observe ended history
        viewModelScope.launch {
            sessionRepository.observeSessionHistory().collectLatest { list ->
                _endedSessions.postValue(list)
            }
        }

        // Observe my active session
        viewModelScope.launch {
            sessionRepository.observeMyActiveSession(uid).collectLatest { session ->
                _myActiveSession.postValue(session)
            }
        }
    }

    fun startSession(name: String, skill: String, platform: String, meetingLink: String, isScheduled: Boolean = false, scheduledTime: Long = 0L) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // 6-digit session code generation
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars.random() }.joinToString("")

        val hostName = FirebaseUtil.currentUser?.displayName ?: "Tutor"

        val session = Session(
            hostId = uid,
            hostName = hostName,
            name = name,
            skill = skill,
            platform = platform,
            platformLabel = platform.uppercase(),
            meetingLink = meetingLink,
            code = code,
            status = if (isScheduled) "scheduled" else "live",
            startTime = if (isScheduled) scheduledTime else System.currentTimeMillis(),
            isScheduled = isScheduled,
            scheduledTime = scheduledTime
        )

        viewModelScope.launch {
            val result = sessionRepository.startSession(session)
            _startResult.postValue(result)
        }
    }

    fun resetStartResult() {
        _startResult.value = null
    }

    fun joinSession(code: String) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val result = sessionRepository.joinSession(code.uppercase(), uid)
            _joinResult.postValue(result)
        }
    }

    fun resetJoinResult() {
        _joinResult.value = null
    }

    fun endSession(session: Session) {
        val durationMins = (System.currentTimeMillis() - session.startTime) / 60000
        viewModelScope.launch {
            val result = sessionRepository.endSession(session.id, durationMins)
            _actionResult.postValue(result)
        }
    }

    fun submitFeedback(sessionId: String, text: String) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return
        val hostName = FirebaseUtil.currentUser?.displayName ?: "Student"

        viewModelScope.launch {
            val feedback = SessionFeedback(uid, hostName, text, System.currentTimeMillis())
            val result = sessionRepository.submitFeedback(sessionId, feedback)
            _actionResult.postValue(result)
        }
    }

    fun submitRating(sessionId: String, stars: Long) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return
        val hostName = FirebaseUtil.currentUser?.displayName ?: "Student"

        viewModelScope.launch {
            val rating = SessionRating(uid, hostName, stars, System.currentTimeMillis())
            val result = sessionRepository.submitRating(sessionId, rating)
            _actionResult.postValue(result)
        }
    }

    fun resetActionResult() {
        _actionResult.value = null
    }
}
