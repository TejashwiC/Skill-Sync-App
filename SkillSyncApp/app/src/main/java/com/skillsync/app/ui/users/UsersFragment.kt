package com.skillsync.app.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentUsersBinding
import com.skillsync.app.ui.adapter.UserAdapter
import com.skillsync.app.util.showToast

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = UserAdapter(
            onFollowClick = { user ->
                viewModel.toggleFollowUser(user)
            },
            onViewProfileClick = { user ->
                val bundle = Bundle().apply {
                    putString("userId", user.uid)
                }
                findNavController().navigate(R.id.nav_user_profile, bundle)
            }
        )
        binding.rvUsersList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsersList.adapter = adapter

        // Observers
        viewModel.allUsers.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Follow Status Updated!")
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
            }
        }

        // Navigation
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.nav_search_users)
        }

        binding.btnFollowers.setOnClickListener {
            findNavController().navigate(R.id.nav_followers)
        }

        binding.btnFollowing.setOnClickListener {
            findNavController().navigate(R.id.nav_following)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
