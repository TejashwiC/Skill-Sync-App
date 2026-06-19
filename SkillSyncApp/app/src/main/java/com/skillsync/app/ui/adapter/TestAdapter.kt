package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.Test
import com.skillsync.app.databinding.ItemTestBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class TestAdapter(
    private var tests: List<Test> = emptyList(),
    private val onStartClick: (Test) -> Unit,
    private val onDeleteClick: (Test) -> Unit
) : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    fun submitList(newTests: List<Test>) {
        tests = newTests
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val binding = ItemTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(tests[position])
    }

    override fun getItemCount(): Int = tests.size

    inner class TestViewHolder(private val binding: ItemTestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(test: Test) {
            binding.tvTestTitle.text = test.title
            binding.tvTestSkill.text = "Skill: ${test.skill}"
            binding.tvTestCreator.text = "By: ${test.creatorName}"
            binding.tvTestCredits.text = "Reward: ${test.credits} Credits"

            val currentUid = FirebaseUtil.currentUid
            val isCreator = test.creatorId == currentUid

            if (isCreator) {
                binding.btnDeleteTest.show()
                binding.btnStartTest.hide()
            } else {
                binding.btnDeleteTest.hide()
                binding.btnStartTest.show()
            }

            binding.btnStartTest.setOnClickListener { onStartClick(test) }
            binding.btnDeleteTest.setOnClickListener { onDeleteClick(test) }
        }
    }
}
