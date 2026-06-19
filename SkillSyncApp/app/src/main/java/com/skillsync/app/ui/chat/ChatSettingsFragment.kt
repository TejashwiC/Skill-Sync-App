package com.skillsync.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.ChatRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.databinding.FragmentChatSettingsBinding
import com.skillsync.app.databinding.ItemBlockedUserBinding
import com.skillsync.app.databinding.ItemManageChatBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class ChatSettingsFragment : Fragment() {

    private var _binding: FragmentChatSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val userRepository = UserRepository()
    private val chatRepository = ChatRepository()

    private lateinit var blockedAdapter: BlockedUsersAdapter
    private lateinit var manageChatsAdapter: ManageChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup switch for notifications
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.switchChatNotif.isChecked = it.chatNotifications
            }
        }
        binding.switchChatNotif.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userRepository.updateNotificationSetting(FirebaseUtil.currentUid, "chatNotifications", isChecked)
                showToast("Chat notifications turned ${if (isChecked) "ON" else "OFF"}")
            }
        }

        // 2. Setup Blocked Users list
        blockedAdapter = BlockedUsersAdapter { user ->
            lifecycleScope.launch {
                userRepository.unblockUser(FirebaseUtil.currentUid, user.uid)
                showToast("User Unblocked!")
            }
        }
        binding.rvBlockedUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBlockedUsers.adapter = blockedAdapter

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                lifecycleScope.launch {
                    val list = mutableListOf<User>()
                    for (uid in it.blocked) {
                        userRepository.getUserProfile(uid).getOrNull()?.let { list.add(it) }
                    }
                    if (list.isEmpty()) {
                        binding.tvNoBlocked.show()
                        binding.rvBlockedUsers.hide()
                    } else {
                        binding.tvNoBlocked.hide()
                        binding.rvBlockedUsers.show()
                        blockedAdapter.submitList(list)
                    }
                }
            }
        }

        // 3. Setup Manage Chats list
        manageChatsAdapter = ManageChatsAdapter { item ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Chat")
                .setMessage("Delete entire chat with ${item.otherUser.name}? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        val chatId = chatRepository.getChatId(FirebaseUtil.currentUid, item.otherUser.uid)
                        val result = chatRepository.deleteChat(chatId)
                        if (result.isSuccess) {
                            showToast("Chat deleted!")
                            viewModel.loadInbox() // Refresh list
                        } else {
                            showToast("Failed to delete chat")
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.rvManageChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManageChats.adapter = manageChatsAdapter

        viewModel.inbox.observe(viewLifecycleOwner) { list ->
            manageChatsAdapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inline Adapter for Blocked Users
    inner class BlockedUsersAdapter(
        private var list: List<User> = emptyList(),
        private val onUnblockClick: (User) -> Unit
    ) : RecyclerView.Adapter<BlockedUsersAdapter.BlockedViewHolder>() {

        fun submitList(newList: List<User>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
            val binding = ItemBlockedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BlockedViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
            val user = list[position]
            holder.binding.tvBlockedName.text = user.name
            holder.binding.btnUnblock.setOnClickListener { onUnblockClick(user) }
        }

        override fun getItemCount(): Int = list.size

        inner class BlockedViewHolder(val binding: ItemBlockedUserBinding) : RecyclerView.ViewHolder(binding.root)
    }

    // Inline Adapter for Chat History Deletion
    inner class ManageChatsAdapter(
        private var list: List<ChatViewModel.InboxItem> = emptyList(),
        private val onDeleteClick: (ChatViewModel.InboxItem) -> Unit
    ) : RecyclerView.Adapter<ManageChatsAdapter.ManageViewHolder>() {

        fun submitList(newList: List<ChatViewModel.InboxItem>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageViewHolder {
            val binding = ItemManageChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ManageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ManageViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvManageChatName.text = item.otherUser.name
            holder.binding.btnDeleteChat.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = list.size

        inner class ManageViewHolder(val binding: ItemManageChatBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
