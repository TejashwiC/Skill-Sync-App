package com.skillsync.app.ui.session

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.databinding.FragmentLiveSessionsBinding
import com.skillsync.app.ui.adapter.SessionAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class LiveSessionsFragment : Fragment() {

    private var _binding: FragmentLiveSessionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private lateinit var adapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SessionAdapter(
            onJoinClick = { session ->
                openMeetingLink(session.meetingLink)
            },
            onEndClick = { session ->
                viewModel.endSession(session)
            }
        )
        binding.rvLiveSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLiveSessions.adapter = adapter

        viewModel.liveSessions.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyLive.show()
                binding.rvLiveSessions.hide()
            } else {
                binding.tvEmptyLive.hide()
                binding.rvLiveSessions.show()
                adapter.submitList(list)
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Session Status Updated!")
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
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
