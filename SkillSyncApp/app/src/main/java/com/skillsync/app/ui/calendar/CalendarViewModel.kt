package com.skillsync.app.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.Session
import com.skillsync.app.data.repository.SessionRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {
    private val sessionRepository = SessionRepository()

    private val _calendarSessions = MutableLiveData<List<Session>>()
    val calendarSessions: LiveData<List<Session>> = _calendarSessions

    init {
        loadMyCalendarSessions()
    }

    private fun loadMyCalendarSessions() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            sessionRepository.observeMyCalendarSessions(uid).collectLatest { list ->
                _calendarSessions.postValue(list)
            }
        }
    }
}
