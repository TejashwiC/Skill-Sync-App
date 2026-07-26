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

    private val _createdEndedSessions = MutableLiveData<List<Session>>()
    val createdEndedSessions: LiveData<List<Session>> = _createdEndedSessions

    private val _attendedEndedSessions = MutableLiveData<List<Session>>()
    val attendedEndedSessions: LiveData<List<Session>> = _attendedEndedSessions

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

        // Observe created ended sessions for logged in user
        viewModelScope.launch {
            sessionRepository.observeMyCreatedEndedSessions(uid).collectLatest { list ->
                _createdEndedSessions.postValue(list)
            }
        }

        // Observe attended ended sessions for logged in user
        viewModelScope.launch {
            sessionRepository.observeMyAttendedEndedSessions(uid).collectLatest { list ->
                _attendedEndedSessions.postValue(list)
            }
        }

        // Observe current user profile to get following/followers for mutual connection filter
        viewModelScope.launch {
            val userRepo = com.skillsync.app.data.repository.UserRepository()
            userRepo.observeUserProfile(uid).collectLatest { myProfile ->
                val following = myProfile?.following ?: emptyList()
                val followers = myProfile?.followers ?: emptyList()
                // Mutual connections: people I follow who also follow me back
                val mutualConnections = following.filter { followers.contains(it) }

                // Observe live sessions - visible to self + mutual connections
                viewModelScope.launch {
                    sessionRepository.observeLiveSessions().collectLatest { list ->
                        val filtered = list.filter { session ->
                            session.hostId == uid || mutualConnections.contains(session.hostId)
                        }
                        _liveSessions.postValue(filtered)
                    }
                }

                // Observe ended history - visible to self + mutual connections
                viewModelScope.launch {
                    sessionRepository.observeSessionHistory().collectLatest { list ->
                        val filtered = list.filter { session ->
                            session.hostId == uid || mutualConnections.contains(session.hostId)
                        }
                        _endedSessions.postValue(filtered)
                    }
                }
            }
        }

        // Observe my active session
        viewModelScope.launch {
            sessionRepository.observeMyActiveSession(uid).collectLatest { session ->
                _myActiveSession.postValue(session)
            }
        }
    }

    fun startSession(name: String, skill: String, platform: String, meetingLink: String, isScheduled: Boolean = false, scheduledTime: Long = 0L, durationMins: Long = 60L) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // 6-digit session code generation
        val code = (1..6).map { ('0'..'9').random() }.joinToString("")

        val hostName = FirebaseUtil.currentUser?.displayName ?: "Tutor"

        val startTimeMs = if (isScheduled) scheduledTime else System.currentTimeMillis()
        val endTimeMs = startTimeMs + (durationMins * 60 * 1000L)

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
            startTime = startTimeMs,
            endTime = endTimeMs,
            durationMins = durationMins,
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

    fun launchScheduledSession(session: Session) {
        viewModelScope.launch {
            val result = sessionRepository.launchScheduledSession(session.id)
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
