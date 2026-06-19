package com.skillsync.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.User
import com.skillsync.app.databinding.FragmentBlockedUsersBinding
import com.skillsync.app.databinding.ItemBlockedUserBinding
import com.skillsync.app.ui.users.UsersViewModel
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class BlockedUsersFragment : Fragment() {

    private var _binding: FragmentBlockedUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: BlockedUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarBlockedUsers.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = BlockedUserAdapter { user ->
            viewModel.toggleBlockUser(user)
        }
        binding.rvBlockedUsers.adapter = adapter

        viewModel.blockedUsers.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.rvBlockedUsers.hide()
                binding.tvEmptyBlockedUsers.show()
            } else {
                binding.rvBlockedUsers.show()
                binding.tvEmptyBlockedUsers.hide()
                adapter.submitList(list)
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("User unblocked")
                } else {
                    showToast("Failed to unblock user")
                }
                viewModel.resetActionResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class BlockedUserAdapter(
        private val onUnblockClick: (User) -> Unit
    ) : RecyclerView.Adapter<BlockedUserAdapter.ViewHolder>() {

        private var users: List<User> = emptyList()

        fun submitList(newList: List<User>) {
            users = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemBlockedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.binding.tvBlockedName.text = user.name
            holder.binding.btnUnblock.setOnClickListener {
                onUnblockClick(user)
            }
        }

        override fun getItemCount(): Int = users.size

        inner class ViewHolder(val binding: ItemBlockedUserBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
