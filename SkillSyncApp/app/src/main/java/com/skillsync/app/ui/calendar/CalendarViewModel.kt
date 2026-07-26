package com.skillsync.app.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.Session
import com.skillsync.app.data.repository.SessionRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {
    private val sessionRepository = SessionRepository()
    private val userRepository = UserRepository()

    private val _calendarSessions = MutableLiveData<List<Session>>()
    val calendarSessions: LiveData<List<Session>> = _calendarSessions

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private var sessionJob: Job? = null

    init {
        loadMyCalendarSessions()
    }

    private fun loadMyCalendarSessions() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // Watch user profile changes (following/followers updates)
        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                val following = user?.following ?: emptyList()
                val followers = user?.followers ?: emptyList()

                // Cancel previous session subscription and start fresh with updated connections
                sessionJob?.cancel()
                sessionJob = viewModelScope.launch {
                    sessionRepository.observeMyCalendarSessions(uid, following, followers)
                        .collectLatest { list ->
                            _calendarSessions.postValue(list)
                        }
                }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val result = sessionRepository.deleteSession(sessionId)
            _deleteResult.postValue(result)
        }
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }
}
