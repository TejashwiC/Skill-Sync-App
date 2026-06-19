package com.skillsync.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentChatBinding
import com.skillsync.app.ui.adapter.InboxAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InboxAdapter { item ->
            val bundle = Bundle().apply {
                putString("userId", item.otherUser.uid)
                putString("userName", item.otherUser.name)
            }
            findNavController().navigate(R.id.nav_chat_conversation, bundle)
        }
        val groupAdapter = com.skillsync.app.ui.adapter.GroupAdapter { group ->
            val bundle = Bundle().apply {
                putString("userId", group.id) // Using userId param to hold groupId
                putString("userName", group.name)
                putBoolean("isGroup", true)
            }
            findNavController().navigate(R.id.nav_chat_conversation, bundle)
        }
        
        binding.rvInbox.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInbox.adapter = adapter

        viewModel.inbox.observe(viewLifecycleOwner) { list ->
            if (binding.tabLayoutChats.selectedTabPosition == 0) {
                if (list.isEmpty()) {
                    binding.tvEmptyInbox.text = "No private conversations yet."
                    binding.tvEmptyInbox.show()
                    binding.rvInbox.hide()
                } else {
                    binding.tvEmptyInbox.hide()
                    binding.rvInbox.show()
                    adapter.submitList(list)
                }
            }
        }

        viewModel.groups.observe(viewLifecycleOwner) { list ->
            if (binding.tabLayoutChats.selectedTabPosition == 1) {
                if (list.isEmpty()) {
                    binding.tvEmptyInbox.text = "No group conversations yet."
                    binding.tvEmptyInbox.show()
                    binding.rvInbox.hide()
                } else {
                    binding.tvEmptyInbox.hide()
                    binding.rvInbox.show()
                    groupAdapter.submitList(list)
                }
            }
        }

        binding.tabLayoutChats.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                if (tab?.position == 0) {
                    binding.rvInbox.adapter = adapter
                    viewModel.loadInbox()
                    binding.btnNewChat.show()
                    binding.btnNewGroup.hide()
                } else {
                    binding.rvInbox.adapter = groupAdapter
                    viewModel.loadGroups()
                    binding.btnNewChat.hide()
                    binding.btnNewGroup.show()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
        
        binding.btnNewGroup.hide() // default
        binding.btnNewGroup.setOnClickListener {
            findNavController().navigate(R.id.nav_create_group)
        }

        binding.etSearchChats.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim().lowercase()
                filterChats(query)
            }
        })

        binding.btnInbox.setOnClickListener {
            if (binding.tabLayoutChats.selectedTabPosition == 0) viewModel.loadInbox() else viewModel.loadGroups()
        }

        binding.btnNewChat.setOnClickListener {
            findNavController().navigate(R.id.nav_inbox)
        }

        binding.btnChatSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_chat_settings)
        }
    }

    private fun filterChats(query: String) {
        if (binding.tabLayoutChats.selectedTabPosition == 0) {
            val currentList = viewModel.inbox.value ?: emptyList()
            val filtered = if (query.isEmpty()) currentList else currentList.filter { 
                it.otherUser.name.lowercase().contains(query) 
            }
            adapter.submitList(filtered)
        } else {
            val currentList = viewModel.groups.value ?: emptyList()
            val filtered = if (query.isEmpty()) currentList else currentList.filter { 
                it.name.lowercase().contains(query) 
            }
            (binding.rvInbox.adapter as com.skillsync.app.ui.adapter.GroupAdapter).submitList(filtered)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInbox()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
