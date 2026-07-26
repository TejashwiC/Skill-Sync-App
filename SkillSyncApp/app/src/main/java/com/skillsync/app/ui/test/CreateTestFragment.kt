package com.skillsync.app.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentCreateTestBinding
import com.skillsync.app.util.QuestionBankHelper
import com.skillsync.app.util.QuestionBankItem
import com.skillsync.app.util.showToast

class CreateTestFragment : Fragment() {

    private var _binding: FragmentCreateTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()
    private var fetchedQuestions: List<QuestionBankItem> = emptyList()

    private val skillsList = listOf(
        "Python", "Java", "C", "C++", "JavaScript",
        "HTML_CSS", "SQL", "WebDevelopment", "ArtificialIntelligence", "DataScience",
        "Cooking", "Painting", "Drawing", "Photography", "Music",
        "Dance", "Fitness", "PublicSpeaking", "Gardening", "Business"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, skillsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSkill.adapter = adapter

        // Fetch Questions Button
        binding.btnFetchQuestions.setOnClickListener {
            val selectedSkill = binding.spinnerSkill.selectedItem?.toString() ?: "Python"
            val selectedDifficulty = when (binding.rgDifficulty.checkedRadioButtonId) {
                R.id.rbMedium -> "Medium"
                R.id.rbHard -> "Hard"
                else -> "Basic"
            }

            fetchedQuestions = QuestionBankHelper.load5RandomQuestions(requireContext(), selectedSkill, selectedDifficulty)

            if (fetchedQuestions.size < 5) {
                showToast("Failed to load questions from Question Bank.")
                binding.containerQuestionsPreview.visibility = View.GONE
                return@setOnClickListener
            }

            renderPreview(fetchedQuestions, selectedSkill)
            binding.containerQuestionsPreview.visibility = View.VISIBLE
        }

        // Publish Test Button
        binding.btnPublishTest.setOnClickListener {
            val selectedSkill = binding.spinnerSkill.selectedItem?.toString() ?: "Python"
            val selectedDifficulty = when (binding.rgDifficulty.checkedRadioButtonId) {
                R.id.rbMedium -> "Medium"
                R.id.rbHard -> "Hard"
                else -> "Basic"
            }

            if (fetchedQuestions.size != 5) {
                showToast("Please fetch 5 random questions first.")
                return@setOnClickListener
            }

            viewModel.publishPreparedTest(selectedSkill, selectedDifficulty, fetchedQuestions)
        }

        // Observe Action Result
        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Test Published Successfully!")
                    viewModel.resetActionResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                    viewModel.resetActionResult()
                }
            }
        }
    }

    private fun renderPreview(questions: List<QuestionBankItem>, skill: String) {
        val container = binding.llQuestionsList
        container.removeAllViews()

        val density = resources.displayMetrics.density

        questions.forEachIndexed { index, q ->
            val card = CardView(requireContext()).apply {
                radius = 12 * density
                cardElevation = 2 * density
                setCardBackgroundColor(Color.WHITE)
                useCompatPadding = true
            }

            val ll = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val p = (14 * density).toInt()
                setPadding(p, p, p, p)
            }

            // Header Row (Question Number & Topic Badge)
            val headerLl = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (8 * density).toInt() }
            }

            val tvNum = TextView(requireContext()).apply {
                text = "Question ${index + 1} of 5"
                setTextColor(Color.parseColor("#1565C0"))
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvBadge = TextView(requireContext()).apply {
                text = q.topic.ifEmpty { skill }
                setTextColor(Color.parseColor("#0369A1"))
                setBackgroundColor(Color.parseColor("#E0F2FE"))
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                val padH = (8 * density).toInt()
                val padV = (3 * density).toInt()
                setPadding(padH, padV, padH, padV)
            }

            headerLl.addView(tvNum)
            headerLl.addView(tvBadge)
            ll.addView(headerLl)

            // Question Text
            val tvQ = TextView(requireContext()).apply {
                text = q.question
                setTextColor(Color.parseColor("#1E293B"))
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (10 * density).toInt() }
            }
            ll.addView(tvQ)

            // Options List
            q.options.forEach { opt ->
                val isCorrect = opt == q.answer
                val tvOpt = TextView(requireContext()).apply {
                    text = if (isCorrect) "$opt  ✓ Correct Answer" else opt
                    setTextColor(if (isCorrect) Color.parseColor("#15803D") else Color.parseColor("#334155"))
                    setBackgroundColor(if (isCorrect) Color.parseColor("#F0FDF4") else Color.parseColor("#F8FAFC"))
                    textSize = 12f
                    if (isCorrect) setTypeface(null, android.graphics.Typeface.BOLD)
                    val pOpt = (8 * density).toInt()
                    setPadding(pOpt, pOpt, pOpt, pOpt)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = (4 * density).toInt() }
                }
                ll.addView(tvOpt)
            }

            // Explanation
            if (q.explanation.isNotEmpty()) {
                val tvExp = TextView(requireContext()).apply {
                    text = "💡 Explanation: ${q.explanation}"
                    setTextColor(Color.parseColor("#64748B"))
                    setBackgroundColor(Color.parseColor("#F1F5F9"))
                    textSize = 11f
                    val pExp = (8 * density).toInt()
                    setPadding(pExp, pExp, pExp, pExp)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = (8 * density).toInt() }
                }
                ll.addView(tvExp)
            }

            card.addView(ll)
            container.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
