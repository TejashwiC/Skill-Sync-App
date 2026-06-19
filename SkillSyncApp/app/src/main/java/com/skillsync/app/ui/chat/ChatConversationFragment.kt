package com.skillsync.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.data.repository.ChatRepository
import com.skillsync.app.databinding.FragmentChatConversationBinding
import com.skillsync.app.ui.adapter.ChatMessageAdapter
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class ChatConversationFragment : Fragment() {

    private var _binding: FragmentChatConversationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val chatRepository = ChatRepository()
    private lateinit var adapter: ChatMessageAdapter

    private var targetUserId: String = ""
    private var targetUserName: String = ""
    private var chatId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        targetUserId = arguments?.getString("userId") ?: return
        targetUserName = arguments?.getString("userName") ?: "Chat"
        val isGroup = arguments?.getBoolean("isGroup", false) ?: false

        if (isGroup) {
            chatId = targetUserId
            binding.tvChatStatus.text = "Group Chat"
        } else {
            chatId = chatRepository.getChatId(FirebaseUtil.currentUid, targetUserId)
            observeTargetUserStatus()
        }

        binding.tvChatWith.text = targetUserName

        // Setup RecyclerView
        adapter = ChatMessageAdapter()
        adapter.onMessagePin = { message ->
            val action = if (message.isPinned) "Unpin" else "Pin"
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("$action Message")
                .setMessage("Do you want to ${action.lowercase()} this message?")
                .setPositiveButton(action) { _, _ ->
                    viewModel.pinMessage(chatId, message.messageId, !message.isPinned)
                    showToast("Message ${action.lowercase()}ned")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        // Setup message observer
        viewModel.observeConversation(chatId)
        viewModel.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNotEmpty()) {
                binding.rvMessages.smoothScrollToPosition(list.size - 1)
            }
        }

        // Check block and connection status only for private chats
        if (!isGroup) {
            lifecycleScope.launch {
                val blockStatus = viewModel.checkBlockStatus(targetUserId)
                val isConnected = viewModel.checkConnectionStatus(targetUserId)
                
                if (blockStatus == ChatViewModel.BlockStatus.I_BLOCKED) {
                    binding.layoutInput.hide()
                    showToast("You have blocked this user.")
                } else if (blockStatus == ChatViewModel.BlockStatus.THEY_BLOCKED) {
                    binding.layoutInput.hide()
                    showToast("You cannot send messages to this user.")
                } else if (!isConnected) {
                    binding.layoutInput.hide()
                    showToast("You must be connected to chat with this person.")
                }
            }
        }

        // Audio recording is disabled as requested
        // setupAudioRecording()
        binding.btnRecordVoice.visibility = android.view.View.GONE

        // Setup Attachment Picker
        binding.btnAttachPdf.setOnClickListener {
            attachmentPickerLauncher.launch("*/*")
        }

        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            sendMessage(text, null, null, null)
        }

        viewModel.sendMessageResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isFailure) {
                    showToast("Failed to send message: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetSendMessageResult()
            }
        }

        setupChatMenu()
        setupChatSearch()
        loadChatTheme()
    }

    private fun setupChatMenu() {
        binding.btnChatMenu.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(com.skillsync.app.R.menu.menu_chat_conversation, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    com.skillsync.app.R.id.action_search -> {
                        binding.etChatSearch.visibility = if (binding.etChatSearch.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        true
                    }
                    com.skillsync.app.R.id.action_theme -> {
                        showThemeDialog()
                        true
                    }
                    com.skillsync.app.R.id.action_shared_media -> {
                        val messages = viewModel.messages.value ?: emptyList()
                        val mediaLinks = messages.filter { it.pdfUrl != null }.map { it.pdfUrl!! }
                        if (mediaLinks.isEmpty()) {
                            showToast("No shared documents in this chat.")
                        } else {
                            val linksArray = mediaLinks.mapIndexed { index, url -> "Document ${index + 1}" }.toTypedArray()
                            android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Shared Documents")
                                .setItems(linksArray) { _, which ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                    intent.data = android.net.Uri.parse(mediaLinks[which])
                                    startActivity(intent)
                                }
                                .show()
                        }
                        true
                    }
                    com.skillsync.app.R.id.action_clear_chat -> {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Clear Chat")
                            .setMessage("Are you sure you want to delete all messages? This action cannot be undone.")
                            .setPositiveButton("Clear") { _, _ ->
                                viewModel.clearChat(chatId)
                                showToast("Chat cleared")
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                    com.skillsync.app.R.id.action_unfriend -> {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete Connection")
                            .setMessage("Are you sure you want to remove this connection?")
                            .setPositiveButton("Delete") { _, _ ->
                                viewModel.unfriendUser(targetUserId)
                                showToast("Connection removed")
                                findNavController().popBackStack()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun setupChatSearch() {
        binding.etChatSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim().lowercase()
                val currentList = viewModel.messages.value ?: emptyList()
                val filtered = if (query.isEmpty()) currentList else currentList.filter { 
                    it.text.lowercase().contains(query) 
                }
                adapter.submitList(filtered)
            }
        })
    }

    private fun showThemeDialog() {
        val colors = arrayOf("Default", "Pink", "Blue", "Dark", "Green")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Chat Theme")
            .setItems(colors) { _, which ->
                val prefs = requireContext().getSharedPreferences("ChatPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("theme_$chatId", colors[which]).apply()
                loadChatTheme()
            }
            .show()
    }

    private fun loadChatTheme() {
        val prefs = requireContext().getSharedPreferences("ChatPrefs", android.content.Context.MODE_PRIVATE)
        val theme = prefs.getString("theme_$chatId", "Default")
        val colorRes = when (theme) {
            "Pink" -> android.graphics.Color.parseColor("#FFE4E1")
            "Blue" -> android.graphics.Color.parseColor("#E0F7FA")
            "Dark" -> android.graphics.Color.parseColor("#303030")
            "Green" -> android.graphics.Color.parseColor("#E8F5E9")
            else -> android.graphics.Color.parseColor("#F5F5F5") // default background
        }
        binding.root.setBackgroundColor(colorRes)
        binding.rvMessages.setBackgroundColor(colorRes)
    }

    private val attachmentPickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val mimeType = requireContext().contentResolver.getType(it)
            val isImage = mimeType?.startsWith("image/") == true
            val type = if (isImage) "image" else "pdf"

            showToast("Uploading attachment...")
            viewModel.uploadChatFile(chatId, it, type) { url, errorMsg ->
                if (url != null) {
                    if (isImage) {
                        sendMessage("", null, null, url)
                    } else {
                        sendMessage("", null, url, null)
                    }
                } else {
                    showToast("Failed to upload attachment: ${errorMsg ?: "Unknown error"}")
                }
            }
        }
    }

    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFilePath: String = ""
    private var isRecording = false

    private fun setupAudioRecording() {
        binding.btnRecordVoice.setOnClickListener {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
                return@setOnClickListener
            }

            if (isRecording) {
                stopRecording()
                binding.btnRecordVoice.setColorFilter(android.graphics.Color.GRAY)
            } else {
                startRecording()
                binding.btnRecordVoice.setColorFilter(android.graphics.Color.RED)
            }
        }
    }

    private fun startRecording() {
        audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/audiorecord_chat.3gp"
        mediaRecorder = android.media.MediaRecorder().apply {
            setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        isRecording = true
        showToast("Recording started...")
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Can happen if stopped immediately after starting
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }

        val file = java.io.File(audioFilePath)
        if (file.exists() && file.length() > 0) {
            showToast("Recording stopped. Uploading...")
            viewModel.uploadChatFile(chatId, android.net.Uri.fromFile(file), "audio") { audioUrl, errorMsg ->
                if (audioUrl != null) {
                    sendMessage("", audioUrl, null, null)
                } else {
                    showToast("Failed to upload audio: ${errorMsg ?: "Unknown error"}")
                }
            }
        } else {
            showToast("Recording failed or was too short.")
        }
    }

    private fun observeTargetUserStatus() {
        viewModel.observeUserStatus(targetUserId).observe(viewLifecycleOwner) { user ->
            if (user != null) {
                if (user.isOnline) {
                    binding.tvChatStatus.text = "Online"
                } else {
                    if (user.lastSeen == 0L) {
                        binding.tvChatStatus.text = "Last seen 1 week ago"
                    } else {
                        val now = System.currentTimeMillis()
                        val diff = now - user.lastSeen
                        if (diff < 24 * 60 * 60 * 1000) {
                            val time = android.text.format.DateFormat.format("h:mm a", java.util.Date(user.lastSeen)).toString()
                            binding.tvChatStatus.text = "Last seen today at $time"
                        } else if (diff < 48 * 60 * 60 * 1000) {
                            val time = android.text.format.DateFormat.format("h:mm a", java.util.Date(user.lastSeen)).toString()
                            binding.tvChatStatus.text = "Last seen yesterday at $time"
                        } else {
                            val time = android.text.format.DateFormat.format("MMM dd, h:mm a", java.util.Date(user.lastSeen)).toString()
                            binding.tvChatStatus.text = "Last seen at $time"
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(text: String, audioUrl: String?, pdfUrl: String?, imageUrl: String?) {
        val isGroup = arguments?.getBoolean("isGroup", false) ?: false
        lifecycleScope.launch {
            if (!isGroup) {
                val blockStatus = viewModel.checkBlockStatus(targetUserId)
                val isConnected = viewModel.checkConnectionStatus(targetUserId)
                if (blockStatus != ChatViewModel.BlockStatus.NONE) {
                    showToast("Message could not be sent due to block status.")
                    return@launch
                }
                if (!isConnected) {
                    showToast("Message could not be sent. You must be connected to this user.")
                    return@launch
                }
            }
            viewModel.sendMessage(chatId, text, audioUrl, pdfUrl, imageUrl)
            binding.etMessageInput.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // if (isRecording) {
        //     mediaRecorder?.stop()
        //     mediaRecorder?.release()
        // }
        _binding = null
    }
}
