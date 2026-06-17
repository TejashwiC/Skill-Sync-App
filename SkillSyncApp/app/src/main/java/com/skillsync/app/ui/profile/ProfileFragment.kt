package com.skillsync.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.skillsync.app.R
import com.skillsync.app.data.repository.AuthRepository
import com.skillsync.app.databinding.FragmentProfileBinding
import com.skillsync.app.ui.auth.LoginActivity
import com.skillsync.app.util.showToast

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user profile
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvProfileName.text = it.name
                binding.tvProfileEmail.text = it.email
                if (it.photo.isNotEmpty() && it.photo.length > 10) {
                    binding.ivProfileAvatar.load(it.photo) {
                        placeholder(android.R.drawable.sym_def_app_icon)
                        error(android.R.drawable.sym_def_app_icon)
                    }
                } else {
                    binding.ivProfileAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }

        // Button clicks navigation
        binding.btnNavViewProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_view_profile)
        }

        binding.btnNavEditProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_edit_profile)
        }

        binding.btnNavSkillsOverview.setOnClickListener {
            findNavController().navigate(R.id.nav_skills_overview)
        }

        binding.btnNavAddSkill.setOnClickListener {
            findNavController().navigate(R.id.nav_add_skill)
        }

        binding.btnNavNotes.setOnClickListener {
            findNavController().navigate(R.id.nav_notes)
        }

        binding.btnNavSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            showToast("Logged Out Successfully!")
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
