package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentTestBinding

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()

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

        // Card 1: Prepare Test
        binding.cardPrepareTest.setOnClickListener {
            findNavController().navigate(R.id.nav_create_test)
        }

        // Card 2: Attend Test
        binding.cardAttendTest.setOnClickListener {
            findNavController().navigate(R.id.nav_attend_test)
        }

        // Card 3: Test History
        binding.cardTestHistory.setOnClickListener {
            findNavController().navigate(R.id.nav_test_results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
