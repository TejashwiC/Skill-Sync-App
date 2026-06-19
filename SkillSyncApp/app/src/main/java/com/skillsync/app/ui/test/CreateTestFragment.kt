package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentCreateTestBinding
import com.skillsync.app.util.showToast

class CreateTestFragment : Fragment() {

    private var _binding: FragmentCreateTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateTestSubmit.setOnClickListener {
            val title = binding.etTestTitle.text.toString().trim()
            val skill = binding.etTestSkill.text.toString().trim()
            val creditsStr = binding.etTestCredits.text.toString().trim()

            if (title.isEmpty() || skill.isEmpty() || creditsStr.isEmpty()) {
                showToast("All fields are required")
                return@setOnClickListener
            }

            val credits = creditsStr.toLongOrNull()
            if (credits == null || credits < 0) {
                showToast("Enter a valid credit amount")
                return@setOnClickListener
            }

            viewModel.createTest(title, skill, credits)
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Test Created Successfully!")
                    viewModel.resetActionResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                    viewModel.resetActionResult()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
