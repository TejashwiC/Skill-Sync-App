package com.skillsync.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.databinding.FragmentCreateGroupBinding
import com.skillsync.app.ui.adapter.SelectUserAdapter
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.showToast

class CreateGroupFragment : Fragment() {

    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: SelectUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SelectUserAdapter()
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        viewModel.chatUsers.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.btnCreateGroup.setOnClickListener {
            val groupName = binding.etGroupName.text.toString().trim()
            if (groupName.isEmpty()) {
                showToast("Please enter a group name")
                return@setOnClickListener
            }

            val selectedMembers = adapter.selectedUserIds.toList()
            if (selectedMembers.isEmpty()) {
                showToast("Please select at least one member")
                return@setOnClickListener
            }

            val myUid = FirebaseUtil.currentUid
            val membersWithMe = selectedMembers.toMutableList()
            membersWithMe.add(myUid)

            // Create Group logic in Repository
            val newGroup = com.skillsync.app.data.model.Group(
                name = groupName,
                adminId = myUid,
                members = membersWithMe,
                createdAt = System.currentTimeMillis()
            )

            com.skillsync.app.data.repository.ChatRepository().createGroup(newGroup) { success, groupId ->
                if (success) {
                    showToast("Group created successfully!")
                    findNavController().popBackStack()
                } else {
                    showToast("Failed to create group")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
