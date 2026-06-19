package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.TestAttempt
import com.skillsync.app.databinding.FragmentTestResultsBinding
import com.skillsync.app.databinding.ItemTestAttemptBinding
import com.skillsync.app.util.formatDate
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class TestResultsFragment : Fragment() {

    private var _binding: FragmentTestResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private lateinit var adapter: AttemptsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AttemptsAdapter()
        binding.rvAttempts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAttempts.adapter = adapter

        viewModel.myAttempts.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyResults.show()
                binding.rvAttempts.hide()
            } else {
                binding.tvEmptyResults.hide()
                binding.rvAttempts.show()
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inline RecyclerView Adapter for attempts
    inner class AttemptsAdapter(
        private var list: List<TestAttempt> = emptyList()
    ) : RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder>() {

        fun submitList(newList: List<TestAttempt>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttemptViewHolder {
            val binding = ItemTestAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AttemptViewHolder(binding)
        }

        override fun onBindViewHolder(holder: AttemptViewHolder, position: Int) {
            val attempt = list[position]
            holder.binding.tvAttemptTitle.text = attempt.testTitle
            holder.binding.tvAttemptPercentage.text = "${attempt.percentage}%"
            holder.binding.tvAttemptScore.text = "Score: ${attempt.score}/${attempt.total}"
            holder.binding.tvAttemptCredits.text = "+${attempt.earnedCredits} Credits earned"
            
            val dateStr = attempt.attemptedAt?.time?.formatDate() ?: "Recent"
            holder.binding.tvAttemptDate.text = "Date: $dateStr"
        }

        override fun getItemCount(): Int = list.size

        inner class AttemptViewHolder(val binding: ItemTestAttemptBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
