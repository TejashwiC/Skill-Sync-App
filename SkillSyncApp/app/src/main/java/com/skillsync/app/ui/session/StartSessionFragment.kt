package com.skillsync.app.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentStartSessionBinding
import com.skillsync.app.util.Constants
import com.skillsync.app.util.showToast

class StartSessionFragment : Fragment() {

    private var _binding: FragmentStartSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Platform Spinner
        val platforms = Constants.PLATFORMS.map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPlatform.adapter = adapter

        binding.btnStartSubmit.setOnClickListener {
            val name = binding.etSessionName.text.toString().trim()
            val skill = binding.etSessionSkill.text.toString().trim()
            val platformLabel = binding.spPlatform.selectedItem.toString()
            val meetingLink = binding.etSessionLink.text.toString().trim()

            // Find platform ID
            val platformId = Constants.PLATFORMS.find { it.label == platformLabel }?.id ?: "other"

            if (name.isEmpty() || meetingLink.isEmpty()) {
                showToast("Session name and meeting link are required")
                return@setOnClickListener
            }
            if (!meetingLink.startsWith("http")) {
                showToast("Meeting link must start with http:// or https://")
                return@setOnClickListener
            }

            viewModel.startSession(name, skill, platformId, meetingLink)
        }

        viewModel.startResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Session Launched Successfully!")
                    viewModel.resetStartResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
