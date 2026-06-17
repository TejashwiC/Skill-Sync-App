package com.skillsync.app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.ChatMessage
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.ChatRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val userRepository = UserRepository()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _inbox = MutableLiveData<List<InboxItem>>()
    val inbox: LiveData<List<InboxItem>> = _inbox

    private val _chatUsers = MutableLiveData<List<User>>()
    val chatUsers: LiveData<List<User>> = _chatUsers

    private val _sendMessageResult = MutableLiveData<Result<Unit>?>()
    val sendMessageResult: LiveData<Result<Unit>?> = _sendMessageResult

    init {
        loadChatUsers()
        loadInbox()
    }

    private fun loadChatUsers() {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        viewModelScope.launch {
            userRepository.observeAllUsers().collectLatest { list ->
                val filtered = list.filter { it.uid != myUid }
                _chatUsers.postValue(filtered)
            }
        }
    }

    fun loadInbox() {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        viewModelScope.launch {
            userRepository.observeAllUsers().collectLatest { users ->
                val items = mutableListOf<InboxItem>()
                for (otherUser in users) {
                    if (otherUser.uid == myUid) continue
                    val chatId = chatRepository.getChatId(myUid, otherUser.uid)
                    val lastMsg = chatRepository.getLatestMessage(chatId)
                    if (lastMsg != null) {
                        items.add(InboxItem(otherUser, lastMsg))
                    }
                }
                // Sort by latest message time
                items.sortByDescending { it.lastMessage.time }
                _inbox.postValue(items)
            }
        }
    }

    fun observeConversation(chatId: String) {
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collectLatest { list ->
                _messages.postValue(list)
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        viewModelScope.launch {
            val result = chatRepository.sendMessage(chatId, text, myUid)
            _sendMessageResult.postValue(result)
            loadInbox() // Refresh inbox list
        }
    }

    fun resetSendMessageResult() {
        _sendMessageResult.value = null
    }

    suspend fun checkBlockStatus(otherUid: String): BlockStatus {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return BlockStatus.NONE

        val myProfile = userRepository.getUserProfile(myUid).getOrNull()
        val otherProfile = userRepository.getUserProfile(otherUid).getOrNull()

        return when {
            myProfile?.blocked?.contains(otherUid) == true -> BlockStatus.I_BLOCKED
            otherProfile?.blocked?.contains(myUid) == true -> BlockStatus.THEY_BLOCKED
            else -> BlockStatus.NONE
        }
    }

    data class InboxItem(
        val otherUser: User,
        val lastMessage: ChatMessage
    )

    enum class BlockStatus {
        NONE, I_BLOCKED, THEY_BLOCKED
    }
}
