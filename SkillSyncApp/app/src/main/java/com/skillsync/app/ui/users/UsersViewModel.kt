package com.skillsync.app.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    private val _followers = MutableLiveData<List<User>>()
    val followers: LiveData<List<User>> = _followers

    private val _following = MutableLiveData<List<User>>()
    val following: LiveData<List<User>> = _following

    private val _actionResult = MutableLiveData<Result<Unit>?>()
    val actionResult: LiveData<Result<Unit>?> = _actionResult

    init {
        loadData()
    }

    private fun loadData() {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        // 1. Observe current user doc
        viewModelScope.launch {
            userRepository.observeUserProfile(myUid).collectLatest { user ->
                _currentUser.postValue(user)
                user?.let {
                    loadFollowersList(it.followers)
                    loadFollowingList(it.following)
                    loadBlockedUsersList(it.blocked)
                }
            }
        }

        // 2. Observe all users list
        viewModelScope.launch {
            userRepository.observeAllUsers().collectLatest { list ->
                // Filter out current user
                val filtered = list.filter { it.uid != myUid }
                _allUsers.postValue(filtered)
            }
        }
    }

    private val _blockedUsers = MutableLiveData<List<User>>()
    val blockedUsers: LiveData<List<User>> = _blockedUsers

    private fun loadFollowersList(uids: List<String>) {
        viewModelScope.launch {
            val list = mutableListOf<User>()
            for (uid in uids) {
                userRepository.getUserProfile(uid).getOrNull()?.let { list.add(it) }
            }
            _followers.postValue(list)
        }
    }

    private fun loadFollowingList(uids: List<String>) {
        viewModelScope.launch {
            val list = mutableListOf<User>()
            for (uid in uids) {
                userRepository.getUserProfile(uid).getOrNull()?.let { list.add(it) }
            }
            _following.postValue(list)
        }
    }

    private fun loadBlockedUsersList(uids: List<String>) {
        viewModelScope.launch {
            val list = mutableListOf<User>()
            for (uid in uids) {
                userRepository.getUserProfile(uid).getOrNull()?.let { list.add(it) }
            }
            _blockedUsers.postValue(list)
        }
    }

    fun toggleFollowUser(targetUser: User) {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        val isFollowing = _currentUser.value?.following?.contains(targetUser.uid) == true
        viewModelScope.launch {
            val result = if (isFollowing) {
                userRepository.unfollowUser(myUid, targetUser.uid)
            } else {
                userRepository.followUser(myUid, targetUser.uid)
            }
            _actionResult.postValue(result)
        }
    }

    fun toggleBlockUser(targetUser: User) {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        val isBlocked = _currentUser.value?.blocked?.contains(targetUser.uid) == true
        viewModelScope.launch {
            val result = if (isBlocked) {
                userRepository.unblockUser(myUid, targetUser.uid)
            } else {
                userRepository.blockUser(myUid, targetUser.uid)
            }
            _actionResult.postValue(result)
        }
    }

    fun resetActionResult() {
        _actionResult.value = null
    }
}
