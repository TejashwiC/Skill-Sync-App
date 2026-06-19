package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.databinding.FragmentLeaderboardBinding
import com.skillsync.app.databinding.ItemLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        viewModel.loadLeaderboard()
        viewModel.leaderboard.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inline RecyclerView Adapter for Leaderboard Rows
    inner class LeaderboardAdapter(
        private var list: List<TestViewModel.LeaderboardRow> = emptyList()
    ) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

        fun submitList(newList: List<TestViewModel.LeaderboardRow>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
            val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LeaderboardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
            val row = list[position]
            holder.binding.tvLeaderboardRank.text = (position + 1).toString()
            holder.binding.tvLeaderboardName.text = row.userName
            holder.binding.tvLeaderboardCredits.text = "${row.earnedCredits} Credits"
        }

        override fun getItemCount(): Int = list.size

        inner class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
