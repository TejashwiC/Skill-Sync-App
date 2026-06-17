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
        binding.rvInbox.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInbox.adapter = adapter

        viewModel.inbox.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyInbox.show()
                binding.rvInbox.hide()
            } else {
                binding.tvEmptyInbox.hide()
                binding.rvInbox.show()
                adapter.submitList(list)
            }
        }

        binding.btnInbox.setOnClickListener {
            viewModel.loadInbox()
        }

        binding.btnNewChat.setOnClickListener {
            findNavController().navigate(R.id.nav_inbox)
        }

        binding.btnChatSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_chat_settings)
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
