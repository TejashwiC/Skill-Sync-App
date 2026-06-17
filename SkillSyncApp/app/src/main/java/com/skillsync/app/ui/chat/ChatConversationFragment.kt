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
        chatId = chatRepository.getChatId(FirebaseUtil.currentUid, targetUserId)

        binding.tvChatWith.text = targetUserName

        // Setup RecyclerView
        adapter = ChatMessageAdapter()
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

        // Check block status
        lifecycleScope.launch {
            val blockStatus = viewModel.checkBlockStatus(targetUserId)
            if (blockStatus == ChatViewModel.BlockStatus.I_BLOCKED) {
                binding.layoutInput.hide()
                showToast("You have blocked this user.")
            } else if (blockStatus == ChatViewModel.BlockStatus.THEY_BLOCKED) {
                binding.layoutInput.hide()
                showToast("You cannot send messages to this user.")
            }
        }

        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            lifecycleScope.launch {
                val blockStatus = viewModel.checkBlockStatus(targetUserId)
                if (blockStatus != ChatViewModel.BlockStatus.NONE) {
                    showToast("Message could not be sent due to block status.")
                    return@launch
                }
                viewModel.sendMessage(chatId, text)
                binding.etMessageInput.text?.clear()
            }
        }

        viewModel.sendMessageResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isFailure) {
                    showToast("Failed to send message: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetSendMessageResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
