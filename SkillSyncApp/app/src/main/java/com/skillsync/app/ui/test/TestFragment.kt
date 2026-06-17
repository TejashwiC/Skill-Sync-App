package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentTestBinding
import com.skillsync.app.ui.adapter.TestAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private lateinit var adapter: TestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TestAdapter(
            onStartClick = {},
            onDeleteClick = { test ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Test")
                    .setMessage("Delete this test and ALL its questions? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTest(test.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvMyTests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyTests.adapter = adapter

        viewModel.myTests.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyTests.show()
                binding.rvMyTests.hide()
            } else {
                binding.tvEmptyTests.hide()
                binding.rvMyTests.show()
                adapter.submitList(list)
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Action Successful!")
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
            }
        }

        // Navigation
        binding.btnCreateTestHub.setOnClickListener {
            findNavController().navigate(R.id.nav_create_test)
        }

        binding.btnAddQuestionHub.setOnClickListener {
            findNavController().navigate(R.id.nav_add_question)
        }

        binding.btnAttendTestHub.setOnClickListener {
            findNavController().navigate(R.id.nav_attend_test)
        }

        binding.btnResultsHub.setOnClickListener {
            findNavController().navigate(R.id.nav_test_results)
        }

        binding.btnLeaderboardHub.setOnClickListener {
            findNavController().navigate(R.id.nav_leaderboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
