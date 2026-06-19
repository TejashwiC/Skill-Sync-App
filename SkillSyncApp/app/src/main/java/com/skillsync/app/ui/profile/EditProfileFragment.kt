package com.skillsync.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentEditProfileBinding
import com.skillsync.app.util.showToast

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill profile fields
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                if (binding.etEditName.text.isNullOrEmpty()) binding.etEditName.setText(it.name)
                if (binding.etEditEmail.text.isNullOrEmpty()) binding.etEditEmail.setText(it.email)
                if (binding.etEditMobile.text.isNullOrEmpty()) binding.etEditMobile.setText(it.mobile)
                if (binding.etEditTeach.text.isNullOrEmpty()) binding.etEditTeach.setText(it.teach)
                if (binding.etEditLearn.text.isNullOrEmpty()) binding.etEditLearn.setText(it.learn)
                if (binding.etEditLanguage.text.isNullOrEmpty()) binding.etEditLanguage.setText(it.language)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString().trim()
            val email = binding.etEditEmail.text.toString().trim()
            val mobile = binding.etEditMobile.text.toString().trim()
            val teach = binding.etEditTeach.text.toString().trim()
            val learn = binding.etEditLearn.text.toString().trim()
            val language = binding.etEditLanguage.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                showToast("Name and email are required")
                return@setOnClickListener
            }

            viewModel.updateProfile(name, email, mobile, teach, learn, language)
        }

        // Observe write status
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Profile Updated Successfully!")
                    viewModel.resetUpdateResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error updating profile: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
