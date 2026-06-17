package com.skillsync.app.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.skillsync.app.R
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.databinding.FragmentUserProfileBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()
    private val userRepository = UserRepository()
    private var targetUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetUserId = arguments?.getString("userId") ?: return

        lifecycleScope.launch {
            val result = userRepository.getUserProfile(targetUserId)
            if (result.isSuccess) {
                targetUser = result.getOrNull()
                bindTargetUser()
            } else {
                showToast("Failed to load user profile")
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            currentUser?.let { myProfile ->
                if (myProfile.following.contains(targetUserId)) {
                    binding.btnProfileFollow.text = "Unfollow"
                } else {
                    binding.btnProfileFollow.text = "Follow"
                }
            }
        }

        binding.btnProfileFollow.setOnClickListener {
            targetUser?.let {
                viewModel.toggleFollowUser(it)
            }
        }

        binding.btnProfileChat.setOnClickListener {
            targetUser?.let {
                val bundle = Bundle().apply {
                    putString("userId", it.uid)
                    putString("userName", it.name)
                }
                findNavController().navigate(R.id.nav_chat_conversation, bundle)
            }
        }
    }

    private fun bindTargetUser() {
        targetUser?.let {
            binding.tvUserTitle.text = "${it.name}'s Profile"
            binding.tvUserProfileName.text = it.name
            binding.tvUserProfileEmail.text = it.email
            binding.tvUserProfileTeach.text = if (it.teach.isNotEmpty()) it.teach else "None"
            binding.tvUserProfileLearn.text = if (it.learn.isNotEmpty()) it.learn else "None"
            binding.tvUserProfileLanguage.text = if (it.language.isNotEmpty()) it.language else "Not Specified"

            if (it.photo.isNotEmpty() && it.photo.length > 10) {
                binding.ivUserProfilePhoto.load(it.photo) {
                    placeholder(android.R.drawable.sym_def_app_icon)
                    error(android.R.drawable.sym_def_app_icon)
                }
            } else {
                binding.ivUserProfilePhoto.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
