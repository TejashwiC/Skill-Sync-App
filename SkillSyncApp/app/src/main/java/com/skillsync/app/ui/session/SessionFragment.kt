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
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentSessionBinding
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class SessionFragment : Fragment() {

    private var _binding: FragmentSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe my active live session
        viewModel.myActiveSession.observe(viewLifecycleOwner) { session ->
            if (session != null) {
                binding.cardActiveSession.show()
                binding.tvNoActive.hide()
                binding.tvActiveName.text = session.name
                binding.tvActivePlatform.text = "Platform: ${session.platformLabel}"
                binding.tvActiveCode.text = session.code

                binding.btnEndActive.setOnClickListener {
                    viewModel.endSession(session)
                }

                binding.btnJoinActive.setOnClickListener {
                    openMeeting(session.meetingLink)
                }
            } else {
                binding.cardActiveSession.hide()
                binding.tvNoActive.show()
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Session Ended Successfully!")
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
            }
        }

        // Navigate to sub-sections
        binding.btnStartHub.setOnClickListener {
            findNavController().navigate(R.id.nav_start_session)
        }

        binding.btnJoinHub.setOnClickListener {
            findNavController().navigate(R.id.nav_join_session)
        }

        binding.btnLiveHub.setOnClickListener {
            findNavController().navigate(R.id.nav_live_sessions)
        }

        binding.btnHistoryHub.setOnClickListener {
            findNavController().navigate(R.id.nav_session_history)
        }

        binding.btnFeedbackHub.setOnClickListener {
            findNavController().navigate(R.id.nav_ratings)
        }
    }

    private fun openMeeting(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Failed to open link: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
