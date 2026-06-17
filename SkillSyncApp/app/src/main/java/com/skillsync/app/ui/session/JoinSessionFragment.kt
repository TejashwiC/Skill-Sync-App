package com.skillsync.app.ui.session

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentJoinSessionBinding
import com.skillsync.app.util.showToast

class JoinSessionFragment : Fragment() {

    private var _binding: FragmentJoinSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnJoinSubmit.setOnClickListener {
            val code = binding.etJoinCode.text.toString().trim()
            if (code.length < 6) {
                showToast("Enter a 6-digit session code")
                return@setOnClickListener
            }
            viewModel.joinSession(code)
        }

        viewModel.joinResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val session = it.getOrNull()
                    session?.let { s ->
                        showToast("Code Verified! Opening meeting...")
                        openMeetingLink(s.meetingLink)
                        viewModel.resetJoinResult()
                        findNavController().popBackStack()
                    }
                } else {
                    showToast("Verification Failed: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    private fun openMeetingLink(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Failed to open meeting client: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
