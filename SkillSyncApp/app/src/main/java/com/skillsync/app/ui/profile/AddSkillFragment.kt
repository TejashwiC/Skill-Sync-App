package com.skillsync.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentAddSkillBinding
import com.skillsync.app.util.showToast

class AddSkillFragment : Fragment() {

    private var _binding: FragmentAddSkillBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSkillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddSkill.setOnClickListener {
            val skill = binding.etAddSkill.text.toString().trim()
            if (skill.isEmpty()) {
                showToast("Enter a skill name")
                return@setOnClickListener
            }
            viewModel.addSkill(skill)
        }

        binding.btnEditSkill.setOnClickListener {
            val oldSkill = binding.etEditOldSkill.text.toString().trim()
            val newSkill = binding.etEditNewSkill.text.toString().trim()
            if (oldSkill.isEmpty() || newSkill.isEmpty()) {
                showToast("Fill both old and new skill fields")
                return@setOnClickListener
            }
            viewModel.editSkill(oldSkill, newSkill)
        }

        binding.btnDeleteSkill.setOnClickListener {
            val skill = binding.etDeleteSkill.text.toString().trim()
            if (skill.isEmpty()) {
                showToast("Enter the skill to remove")
                return@setOnClickListener
            }
            viewModel.deleteSkill(skill)
        }

        // Observe write status
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Skills updated successfully!")
                    viewModel.resetUpdateResult()
                    binding.etAddSkill.text?.clear()
                    binding.etEditOldSkill.text?.clear()
                    binding.etEditNewSkill.text?.clear()
                    binding.etDeleteSkill.text?.clear()
                    findNavController().popBackStack()
                } else {
                    showToast("Error updating skills: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
