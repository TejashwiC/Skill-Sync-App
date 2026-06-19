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
import com.skillsync.app.databinding.FragmentFollowingBinding
import com.skillsync.app.ui.adapter.UserAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class FollowingFragment : Fragment() {

    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.rvFollowing.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFollowing.adapter = adapter

        viewModel.following.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyFollowing.show()
                binding.rvFollowing.hide()
            } else {
                binding.tvEmptyFollowing.hide()
                binding.rvFollowing.show()
                adapter.submitList(list)
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
