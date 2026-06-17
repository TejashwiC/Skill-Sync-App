package com.skillsync.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.TestRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val testRepository = TestRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _testsCreatedCount = MutableLiveData<Int>(0)
    val testsCreatedCount: LiveData<Int> = _testsCreatedCount

    private val _testsCompletedCount = MutableLiveData<Int>(0)
    val testsCompletedCount: LiveData<Int> = _testsCompletedCount

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        // 1. Observe User Profile for credits and teaching skills
        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                _userProfile.postValue(user)
            }
        }

        // 2. Observe Tests Created count
        viewModelScope.launch {
            testRepository.observeMyTests(uid).collectLatest { tests ->
                _testsCreatedCount.postValue(tests.size)
            }
        }

        // 3. Observe Test Attempts (completed tests) count
        viewModelScope.launch {
            testRepository.observeMyAttempts(uid).collectLatest { attempts ->
                _testsCompletedCount.postValue(attempts.size)
            }
        }
    }
}
