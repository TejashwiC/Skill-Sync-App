package com.skillsync.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.skillsync.app.databinding.FragmentViewProfileBinding

class ViewProfileFragment : Fragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvViewProfileName.text = it.name
                binding.tvViewProfileEmail.text = it.email
                binding.tvViewProfileMobile.text = if (it.mobile.isNotEmpty()) it.mobile else "Not Provided"
                binding.tvViewProfileTeach.text = if (it.teach.isNotEmpty()) it.teach else "None"
                binding.tvViewProfileLearn.text = if (it.learn.isNotEmpty()) it.learn else "None"
                binding.tvViewProfileLanguage.text = if (it.language.isNotEmpty()) it.language else "Not Specified"
                binding.tvViewProfileCredits.text = "${it.credits} Credits"

                if (it.photo.isNotEmpty() && it.photo.length > 10) {
                    binding.ivViewProfilePhoto.load(it.photo) {
                        placeholder(android.R.drawable.sym_def_app_icon)
                        error(android.R.drawable.sym_def_app_icon)
                    }
                } else {
                    binding.ivViewProfilePhoto.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
