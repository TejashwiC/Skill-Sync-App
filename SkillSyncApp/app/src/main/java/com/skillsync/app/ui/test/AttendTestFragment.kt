package com.skillsync.app.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var examStartTime: Long = 0L

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

        // Setup Available Tests Adapter
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

        binding.btnBackToAvailableTests.setOnClickListener {
            exitToTestList()
        }
    }

    private fun startActiveExam(test: Test) {
        selectedTest = test
        examStartTime = System.currentTimeMillis()

        binding.layoutTestSelection.hide()
        binding.layoutExamResults.hide()
        binding.layoutActiveExam.show()

        binding.tvAttendTitle.text = "Active Skill Test"
        binding.tvActiveTestTitle.text = "${test.title} (${test.skill})"
        binding.tvActiveTestReward.text = "5 Questions | Select 1 option for each"

        binding.llActiveQuestions.removeAllViews()
        radioGroupMap.clear()

        lifecycleScope.launch {
            val result = testRepository.getQuestionsForTest(test.id)
            if (result.isSuccess) {
                examQuestions = result.getOrDefault(emptyList())
                if (examQuestions.isEmpty()) {
                    showToast("This test has no questions.")
                    exitToTestList()
                    return@launch
                }

                val density = resources.displayMetrics.density

                examQuestions.forEachIndexed { index, q ->
                    val cardBinding = ItemExamQuestionBinding.inflate(layoutInflater, binding.llActiveQuestions, false)

                    cardBinding.tvQuestionNum.text = "Question ${index + 1} of ${examQuestions.size}"
                    cardBinding.tvTopicBadge.text = q.topic.ifEmpty { test.skill }
                    cardBinding.tvExamQuestionText.text = q.question

                    val options = listOf(q.option1, q.option2, q.option3, q.option4).filter { it.isNotEmpty() }
                    cardBinding.rbOption1.text = options.getOrNull(0) ?: ""
                    cardBinding.rbOption2.text = options.getOrNull(1) ?: ""
                    cardBinding.rbOption3.text = options.getOrNull(2) ?: ""
                    cardBinding.rbOption4.text = options.getOrNull(3) ?: ""

                    if (options.size < 4) cardBinding.rbOption4.visibility = View.GONE
                    if (options.size < 3) cardBinding.rbOption3.visibility = View.GONE

                    radioGroupMap[q.id] = cardBinding.rgOptions
                    binding.llActiveQuestions.addView(cardBinding.root)
                }
            } else {
                showToast("Failed to fetch questions: ${result.exceptionOrNull()?.message}")
                exitToTestList()
            }
        }
    }

    private fun submitExamAnswers() {
        val test = selectedTest ?: return

        // Verify all answered
        for (q in examQuestions) {
            val rg = radioGroupMap[q.id] ?: continue
            if (rg.checkedRadioButtonId == -1) {
                showToast("Please answer all 5 questions before submitting.")
                return
            }
        }

        val endTime = System.currentTimeMillis()
        val timeTakenSec = Math.max(1L, (endTime - examStartTime) / 1000L)
        val mins = timeTakenSec / 60
        val secs = timeTakenSec % 60
        val timeTakenStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"

        var score = 0L
        val total = examQuestions.size.toLong()
        val reviewItems = mutableListOf<ReviewItem>()

        for (q in examQuestions) {
            val rg = radioGroupMap[q.id]!!
            val checkedId = rg.checkedRadioButtonId
            val rb = rg.findViewById<RadioButton>(checkedId)
            val selectedAnswer = rb?.text?.toString() ?: "Not answered"
            val isCorrect = selectedAnswer == q.correctAnswer
            if (isCorrect) score++

            reviewItems.add(
                ReviewItem(
                    question = q.question,
                    selectedAnswer = selectedAnswer,
                    correctAnswer = q.correctAnswer,
                    explanation = q.explanation,
                    isCorrect = isCorrect
                )
            )
        }

        val wrongAnswers = total - score
        val percentage = if (total > 0L) ((score.toDouble() / total.toDouble()) * 100).toLong() else 0L

        // Save attempt
        viewModel.submitAttemptDetailed(test, score, total, score, wrongAnswers, timeTakenSec, timeTakenStr)

        // Show Results View
        renderExamResults(score, total, wrongAnswers, percentage, timeTakenStr, reviewItems)
    }

    private fun renderExamResults(
        score: Long,
        total: Long,
        wrongAnswers: Long,
        percentage: Long,
        timeTakenStr: String,
        reviewItems: List<ReviewItem>
    ) {
        binding.layoutActiveExam.hide()
        binding.layoutExamResults.show()
        binding.tvAttendTitle.text = "Test Results"

        binding.tvResTotal.text = "$total"
        binding.tvResCorrect.text = "$score"
        binding.tvResWrong.text = "$wrongAnswers"
        binding.tvResPct.text = "$percentage%"
        binding.tvResTimeTaken.text = "⏱️ Time Taken: $timeTakenStr"

        val container = binding.llReviewQuestions
        container.removeAllViews()
        val density = resources.displayMetrics.density

        reviewItems.forEach { item ->
            val card = CardView(requireContext()).apply {
                radius = 10 * density
                cardElevation = 2 * density
                setCardBackgroundColor(if (item.isCorrect) Color.parseColor("#F0FDF4") else Color.parseColor("#FFF5F5"))
                useCompatPadding = true
            }

            val ll = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val p = (12 * density).toInt()
                setPadding(p, p, p, p)
            }

            val tvQ = TextView(requireContext()).apply {
                text = item.question
                setTextColor(Color.parseColor("#1E293B"))
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }

            val tvAns = TextView(requireContext()).apply {
                text = "Your answer: ${item.selectedAnswer} ${if (item.isCorrect) "✅" else "❌"}"
                setTextColor(if (item.isCorrect) Color.parseColor("#166534") else Color.parseColor("#991B1B"))
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            ll.addView(tvQ)
            ll.addView(tvAns)

            if (!item.isCorrect) {
                val tvCorrect = TextView(requireContext()).apply {
                    text = "Correct answer: ${item.correctAnswer}"
                    setTextColor(Color.parseColor("#15803D"))
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = (2 * density).toInt() }
                }
                ll.addView(tvCorrect)
            }

            if (item.explanation.isNotEmpty()) {
                val tvExp = TextView(requireContext()).apply {
                    text = "💡 Explanation: ${item.explanation}"
                    setTextColor(Color.parseColor("#64748B"))
                    setBackgroundColor(Color.WHITE)
                    textSize = 12f
                    val pE = (8 * density).toInt()
                    setPadding(pE, pE, pE, pE)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = (6 * density).toInt() }
                }
                ll.addView(tvExp)
            }

            card.addView(ll)
            container.addView(card)
        }
    }

    private fun exitToTestList() {
        selectedTest = null
        examQuestions = emptyList()
        radioGroupMap.clear()
        binding.layoutActiveExam.hide()
        binding.layoutExamResults.hide()
        binding.layoutTestSelection.show()
        binding.tvAttendTitle.text = "Attend Skill Test"
        viewModel.loadAvailableTests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class ReviewItem(
        val question: String,
        val selectedAnswer: String,
        val correctAnswer: String,
        val explanation: String,
        val isCorrect: Boolean
    )
}
