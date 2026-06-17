package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.Question
import com.skillsync.app.databinding.ItemQuestionBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.hide
import com.skillsync.app.util.show

class QuestionAdapter(
    private var questions: List<Question> = emptyList(),
    private val onDeleteClick: (Question) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    fun submitList(newQuestions: List<Question>) {
        questions = newQuestions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int = questions.size

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.tvQuestionText.text = question.question
            binding.tvOption1.text = "A: ${question.option1}"
            binding.tvOption2.text = "B: ${question.option2}"
            binding.tvOption3.text = "C: ${question.option3}"
            binding.tvOption4.text = "D: ${question.option4}"
            binding.tvCorrectAnswer.text = "Correct: ${question.correctAnswer}"

            val currentUid = FirebaseUtil.currentUid
            val isCreator = question.creatorId == currentUid

            if (isCreator) {
                binding.btnDeleteQuestion.show()
            } else {
                binding.btnDeleteQuestion.hide()
            }

            binding.btnDeleteQuestion.setOnClickListener { onDeleteClick(question) }
        }
    }
}
