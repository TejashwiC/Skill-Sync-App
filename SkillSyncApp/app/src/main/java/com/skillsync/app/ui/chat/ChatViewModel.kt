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

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

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
        viewModelScope.launch {
            userRepository.observeUserProfile(myUid).collectLatest { user ->
                _currentUser.postValue(user)
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
                val context = com.skillsync.app.SkillSyncApp.appContext
                val prefs = context?.getSharedPreferences("ChatPrefs", android.content.Context.MODE_PRIVATE)
                val pinnedSet = prefs?.getStringSet("pinned_chats", mutableSetOf()) ?: mutableSetOf()

                // Sort by pinned status then latest message time
                items.sortWith(Comparator { a, b ->
                    val aPinned = pinnedSet.contains(a.otherUser.uid)
                    val bPinned = pinnedSet.contains(b.otherUser.uid)
                    if (aPinned && !bPinned) -1
                    else if (!aPinned && bPinned) 1
                    else b.lastMessage.time.compareTo(a.lastMessage.time)
                })
                _inbox.postValue(items)
            }
        }
    }

    fun observeConversation(chatId: String) {
        val myUid = FirebaseUtil.currentUid
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collectLatest { list ->
                _messages.postValue(list)
                if (myUid.isNotEmpty()) {
                    chatRepository.markMessagesAsRead(chatId, myUid)
                }
            }
        }
    }

    fun observeUserStatus(uid: String): LiveData<User?> {
        val result = MutableLiveData<User?>()
        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                result.postValue(user)
            }
        }
        return result
    }

    fun sendMessage(chatId: String, text: String, audioUrl: String? = null, pdfUrl: String? = null, imageUrl: String? = null) {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return
        val senderName = _currentUser.value?.name ?: "User"

        viewModelScope.launch {
            val result = chatRepository.sendMessage(chatId, text, myUid, senderName, audioUrl, pdfUrl, imageUrl)
            _sendMessageResult.postValue(result)
            
            // Note: If it's a group chat, we should also update the group's last message time.
            // But for simplicity we might rely on the client or let the listener handle it.
            // Updating group's last message:
            FirebaseUtil.firestore.collection(com.skillsync.app.util.Constants.COLL_GROUPS)
                .document(chatId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        FirebaseUtil.firestore.collection(com.skillsync.app.util.Constants.COLL_GROUPS)
                            .document(chatId).update(
                                "lastMessage", if (text.isNotEmpty()) text else if (audioUrl != null) "Voice Message" else "Document",
                                "lastMessageTime", System.currentTimeMillis()
                            )
                    }
                }

            loadInbox() // Refresh inbox list
            loadGroups() // Refresh groups
        }
    }

    fun uploadChatFile(chatId: String, uri: android.net.Uri, type: String, onComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            val result = chatRepository.uploadChatFile(chatId, uri, type)
            onComplete(result.getOrNull(), result.exceptionOrNull()?.message)
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

    suspend fun checkConnectionStatus(otherUid: String): Boolean {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return false

        val myProfile = userRepository.getUserProfile(myUid).getOrNull() ?: return false
        val following = myProfile.following ?: emptyList()
        val followers = myProfile.followers ?: emptyList()
        return following.contains(otherUid) || followers.contains(otherUid)
    }

    data class InboxItem(
        val otherUser: User,
        val lastMessage: ChatMessage
    )

    private val _groups = MutableLiveData<List<com.skillsync.app.data.model.Group>>()
    val groups: LiveData<List<com.skillsync.app.data.model.Group>> = _groups

    fun loadGroups() {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return

        viewModelScope.launch {
            FirebaseUtil.firestore.collection(com.skillsync.app.util.Constants.COLL_GROUPS)
                .whereArrayContains("members", myUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val groupList = snapshot.documents.mapNotNull { it.toObject(com.skillsync.app.data.model.Group::class.java) }
                    _groups.postValue(groupList.sortedByDescending { it.lastMessageTime })
                }
        }
    }

    fun clearChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            loadInbox()
        }
    }

    fun unfriendUser(targetUid: String) {
        val myUid = FirebaseUtil.currentUid
        if (myUid.isEmpty()) return
        viewModelScope.launch {
            userRepository.unfollowUser(myUid, targetUid)
        }
    }

    fun pinMessage(chatId: String, messageId: String, pinned: Boolean) {
        viewModelScope.launch {
            chatRepository.pinMessage(chatId, messageId, pinned)
        }
    }

    enum class BlockStatus {
        NONE, I_BLOCKED, THEY_BLOCKED
    }
}
