package com.skillsync.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.skillsync.app.databinding.FragmentSkillsOverviewBinding
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class SkillsOverviewFragment : Fragment() {

    private var _binding: FragmentSkillsOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.cgSkills.removeAllViews()
                val skills = it.teach.split(",")
                    .map { s -> s.trim() }
                    .filter { s -> s.isNotEmpty() }

                if (skills.isEmpty()) {
                    binding.tvNoSkills.show()
                } else {
                    binding.tvNoSkills.hide()
                    for (skill in skills) {
                        val chip = Chip(requireContext()).apply {
                            text = skill
                            isClickable = false
                            isCheckable = false
                        }
                        binding.cgSkills.addView(chip)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
