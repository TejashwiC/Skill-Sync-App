package com.skillsync.app.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userSettings = MutableLiveData<User?>()
    val userSettings: LiveData<User?> = _userSettings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                _userSettings.postValue(user)
            }
        }
    }

    private val _actionResult = MutableLiveData<Result<String>?>()
    val actionResult: LiveData<Result<String>?> = _actionResult

    fun updateSetting(fieldName: String, value: Boolean) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            userRepository.updateNotificationSetting(uid, fieldName, value)
        }
    }

    fun updateAccountName(newName: String) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val res = userRepository.updateAccountName(uid, newName)
            if (res.isSuccess) {
                _actionResult.postValue(Result.success("Name updated successfully!"))
            } else {
                _actionResult.postValue(Result.failure(res.exceptionOrNull() ?: Exception("Failed to update name")))
            }
        }
    }

    fun updatePassword(newPass: String) {
        viewModelScope.launch {
            val res = userRepository.updatePassword(newPass)
            if (res.isSuccess) {
                _actionResult.postValue(Result.success("Password updated successfully!"))
            } else {
                _actionResult.postValue(Result.failure(res.exceptionOrNull() ?: Exception("Failed to update password")))
            }
        }
    }

    fun deleteAccount() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val res = userRepository.deleteUserAccount(uid)
            if (res.isSuccess) {
                _actionResult.postValue(Result.success("ACCOUNT_DELETED"))
            } else {
                _actionResult.postValue(Result.failure(res.exceptionOrNull() ?: Exception("Failed to delete account")))
            }
        }
    }

    fun resetActionResult() {
        _actionResult.value = null
    }
}
