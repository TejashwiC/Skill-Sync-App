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
import com.skillsync.app.databinding.FragmentInboxBinding
import com.skillsync.app.ui.adapter.UserAdapter

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reuse UserAdapter to display users list to choose from
        adapter = UserAdapter(
            onFollowClick = {
                // Do nothing or ignore on follow click inside start new chat screen
            },
            onViewProfileClick = { user ->
                val bundle = Bundle().apply {
                    putString("userId", user.uid)
                    putString("userName", user.name)
                }
                findNavController().navigate(R.id.nav_chat_conversation, bundle)
            }
        )
        binding.rvChatUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatUsers.adapter = adapter

        viewModel.chatUsers.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
