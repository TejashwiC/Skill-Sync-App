package com.skillsync.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.data.model.Question
import com.skillsync.app.data.model.Test
import com.skillsync.app.data.repository.TestRepository
import com.skillsync.app.databinding.FragmentAttendTestBinding
import com.skillsync.app.databinding.ItemExamQuestionBinding
import com.skillsync.app.ui.adapter.TestAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class AttendTestFragment : Fragment() {

    private var _binding: FragmentAttendTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private val testRepository = TestRepository()
    private lateinit var adapter: TestAdapter

    private var selectedTest: Test? = null
    private var examQuestions: List<Question> = emptyList()
    private val radioGroupMap = mutableMapOf<String, RadioGroup>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Selection RecyclerView
        adapter = TestAdapter(
            onStartClick = { test ->
                startActiveExam(test)
            },
            onDeleteClick = {}
        )
        binding.rvAvailableTests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableTests.adapter = adapter

        // Load tests from mutual connections
        viewModel.loadAvailableTests()
        viewModel.availableTests.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyAvailable.show()
                binding.rvAvailableTests.hide()
            } else {
                binding.tvEmptyAvailable.hide()
                binding.rvAvailableTests.show()
                adapter.submitList(list)
            }
        }

        binding.btnSubmitExam.setOnClickListener {
            submitExamAnswers()
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Test Submitted Successfully!")
                    viewModel.resetActionResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                    viewModel.resetActionResult()
                }
            }
        }
    }

    private fun startActiveExam(test: Test) {
        selectedTest = test
        binding.layoutTestSelection.hide()
        binding.layoutActiveExam.show()
        binding.tvAttendTitle.text = "Active Test"
        binding.tvActiveTestTitle.text = test.title
        binding.tvActiveTestReward.text = "Earn 10 Credits per correct answer! Total reward: ${test.credits} credits"

        binding.llActiveQuestions.removeAllViews()
        radioGroupMap.clear()

        lifecycleScope.launch {
            val result = testRepository.getQuestionsForTest(test.id)
            if (result.isSuccess) {
                examQuestions = result.getOrDefault(emptyList())
                if (examQuestions.isEmpty()) {
                    showToast("This test has no questions.")
                    exitExam()
                    return@launch
                }
                
                // Inflate exam cards programmatically
                examQuestions.forEachIndexed { index, q ->
                    val cardBinding = ItemExamQuestionBinding.inflate(layoutInflater, binding.llActiveQuestions, false)
                    cardBinding.tvExamQuestionText.text = "${index + 1}. ${q.question}"
                    cardBinding.rbOption1.text = q.option1
                    cardBinding.rbOption2.text = q.option2
                    cardBinding.rbOption3.text = q.option3
                    cardBinding.rbOption4.text = q.option4

                    // Save reference to check answers on submit
                    radioGroupMap[q.id] = cardBinding.rgOptions
                    
                    binding.llActiveQuestions.addView(cardBinding.root)
                }
            } else {
                showToast("Failed to fetch questions: ${result.exceptionOrNull()?.message}")
                exitExam()
            }
        }
    }

    private fun submitExamAnswers() {
        val test = selectedTest ?: return
        var score = 0L
        val total = examQuestions.size.toLong()

        for (q in examQuestions) {
            val rg = radioGroupMap[q.id] ?: continue
            val checkedId = rg.checkedRadioButtonId
            if (checkedId == -1) {
                showToast("Please answer all questions before submitting")
                return
            }
            val rb = rg.findViewById<RadioButton>(checkedId)
            val answer = rb?.text?.toString()
            if (answer == q.correctAnswer) {
                score++
            }
        }

        viewModel.submitAttempt(test, score, total)
    }

    private fun exitExam() {
        selectedTest = null
        examQuestions = emptyList()
        radioGroupMap.clear()
        binding.layoutActiveExam.hide()
        binding.layoutTestSelection.show()
        binding.tvAttendTitle.text = "Attend Skill Test"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
