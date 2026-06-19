package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.data.model.Test
import com.skillsync.app.databinding.FragmentAddQuestionBinding
import com.skillsync.app.ui.adapter.QuestionAdapter
import com.skillsync.app.util.showToast

class AddQuestionFragment : Fragment() {

    private var _binding: FragmentAddQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private lateinit var adapter: QuestionAdapter
    private var testList: List<Test> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Spinner
        viewModel.myTests.observe(viewLifecycleOwner) { list ->
            testList = list
            val labels = list.map { it.title }
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spTestSelect.adapter = spinnerAdapter
        }

        // Setup RecyclerView
        adapter = QuestionAdapter { question ->
            viewModel.deleteQuestion(question.id)
        }
        binding.rvMyQuestions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyQuestions.adapter = adapter

        viewModel.myQuestions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.btnAddQuestion.setOnClickListener {
            val position = binding.spTestSelect.selectedItemPosition
            if (position < 0 || position >= testList.size) {
                showToast("Select a published test")
                return@setOnClickListener
            }

            val testId = testList[position].id
            val question = binding.etQuestionBody.text.toString().trim()
            val option1 = binding.etOption1.text.toString().trim()
            val option2 = binding.etOption2.text.toString().trim()
            val option3 = binding.etOption3.text.toString().trim()
            val option4 = binding.etOption4.text.toString().trim()
            val correctAnswer = binding.etCorrectAnswer.text.toString().trim()

            if (question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || correctAnswer.isEmpty()) {
                showToast("All fields are required")
                return@setOnClickListener
            }

            val options = listOf(option1, option2, option3, option4)
            if (!options.contains(correctAnswer)) {
                showToast("Correct answer must match one of the 4 options exactly")
                return@setOnClickListener
            }

            viewModel.addQuestion(testId, question, option1, option2, option3, option4, correctAnswer)
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Question Saved successfully!")
                    binding.etQuestionBody.text?.clear()
                    binding.etOption1.text?.clear()
                    binding.etOption2.text?.clear()
                    binding.etOption3.text?.clear()
                    binding.etOption4.text?.clear()
                    binding.etCorrectAnswer.text?.clear()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
