package com.skillsync.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _updateResult = MutableLiveData<Result<Unit>?>()
    val updateResult: LiveData<Result<Unit>?> = _updateResult

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return
        
        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                _userProfile.postValue(user)
            }
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        mobile: String,
        teach: String,
        learn: String,
        language: String
    ) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val result = userRepository.updateUserProfile(uid, name, email, mobile, teach, learn, language)
            _updateResult.postValue(result)
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = null
    }

    fun addSkill(newSkill: String) {
        val user = _userProfile.value ?: return
        val currentSkills = user.teach.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        if (!currentSkills.contains(newSkill)) {
            currentSkills.add(newSkill)
            val updatedTeach = currentSkills.joinToString(",")
            updateProfile(user.name, user.email, user.mobile, updatedTeach, user.learn, user.language)
        }
    }

    fun editSkill(oldSkill: String, newSkill: String) {
        val user = _userProfile.value ?: return
        val currentSkills = user.teach.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        val index = currentSkills.indexOf(oldSkill)
        if (index != -1) {
            currentSkills[index] = newSkill
            val updatedTeach = currentSkills.joinToString(",")
            updateProfile(user.name, user.email, user.mobile, updatedTeach, user.learn, user.language)
        }
    }

    fun deleteSkill(skill: String) {
        val user = _userProfile.value ?: return
        val currentSkills = user.teach.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        if (currentSkills.remove(skill)) {
            val updatedTeach = currentSkills.joinToString(",")
            updateProfile(user.name, user.email, user.mobile, updatedTeach, user.learn, user.language)
        }
    }
}
