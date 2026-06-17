package com.skillsync.app.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.data.model.Session
import com.skillsync.app.databinding.FragmentRatingsBinding
import com.skillsync.app.util.showToast

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private var sessionList: List<Session> = emptyList()
    private var selectedRating: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe ended sessions for spinner dropdown
        viewModel.endedSessions.observe(viewLifecycleOwner) { list ->
            sessionList = list
            val labels = list.map { "${it.name} (${it.hostName})" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spFeedbackSession.adapter = adapter
        }

        // Click listeners for stars
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                setRating(index + 1L, stars)
            }
        }

        binding.btnSubmitReview.setOnClickListener {
            val position = binding.spFeedbackSession.selectedItemPosition
            if (position < 0 || position >= sessionList.size) {
                showToast("Select a valid completed session")
                return@setOnClickListener
            }
            if (selectedRating == 0L) {
                showToast("Select a star rating (1-5)")
                return@setOnClickListener
            }

            val session = sessionList[position]
            val feedbackText = binding.etFeedbackText.text.toString().trim()

            // 1. Submit Rating
            viewModel.submitRating(session.id, selectedRating)
            
            // 2. Submit Feedback if present
            if (feedbackText.isNotEmpty()) {
                viewModel.submitFeedback(session.id, feedbackText)
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Review Submitted Successfully!")
                    viewModel.resetActionResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                    viewModel.resetActionResult()
                }
            }
        }
    }

    private fun setRating(rating: Long, stars: List<TextView>) {
        selectedRating = rating
        stars.forEachIndexed { index, textView ->
            textView.text = if (index < rating) "★" else "☆"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
