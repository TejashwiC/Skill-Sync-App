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

    fun updateSetting(fieldName: String, value: Boolean) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            userRepository.updateNotificationSetting(uid, fieldName, value)
        }
    }
}
