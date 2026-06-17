package com.skillsync.app.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.databinding.FragmentSessionHistoryBinding
import com.skillsync.app.ui.adapter.SessionAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class SessionHistoryFragment : Fragment() {

    private var _binding: FragmentSessionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private lateinit var adapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SessionAdapter(
            onJoinClick = {},
            onEndClick = {}
        )
        binding.rvSessionHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessionHistory.adapter = adapter

        viewModel.endedSessions.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyHistory.show()
                binding.rvSessionHistory.hide()
            } else {
                binding.tvEmptyHistory.hide()
                binding.rvSessionHistory.show()
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
