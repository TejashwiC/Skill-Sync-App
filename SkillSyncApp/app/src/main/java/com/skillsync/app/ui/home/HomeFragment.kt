package com.skillsync.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Profile when clicking profile thumbnail
        binding.ivHomeProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_profile)
        }

        // Bind LiveData observers
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvWelcome.text = "Welcome Back 👋 ${it.name}"
                binding.tvStatCredits.text = it.credits.toString()
                
                // Parse teach skills count
                val teachSkills = it.teach.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }
                binding.tvStatSkills.text = teachSkills.size.toString()

                // Load user profile avatar
                if (it.photo.isNotEmpty() && it.photo.length > 10) {
                    if (it.photo.startsWith("data:image")) {
                        try {
                            val base64String = it.photo.substringAfter("base64,")
                            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.ivHomeProfile.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            binding.ivHomeProfile.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(it.name))
                        }
                    } else {
                        binding.ivHomeProfile.load(it.photo) {
                            placeholder(android.R.drawable.sym_def_app_icon)
                            error(android.R.drawable.sym_def_app_icon)
                        }
                    }
                } else {
                    binding.ivHomeProfile.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(it.name))
                }
            }
        }

        viewModel.testsCreatedCount.observe(viewLifecycleOwner) { count ->
            binding.tvStatTestsCreated.text = count.toString()
        }

        viewModel.testsCompletedCount.observe(viewLifecycleOwner) { count ->
            binding.tvStatTestsCompleted.text = count.toString()
        }

        viewModel.hostedSessionsCount.observe(viewLifecycleOwner) { count ->
            binding.tvStatSessions.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
