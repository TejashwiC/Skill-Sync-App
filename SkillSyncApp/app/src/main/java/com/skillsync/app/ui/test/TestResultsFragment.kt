package com.skillsync.app.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.skillsync.app.data.model.TestAttempt
import com.skillsync.app.databinding.FragmentTestResultsBinding
import com.skillsync.app.util.formatDate

class TestResultsFragment : Fragment() {

    private var _binding: FragmentTestResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by viewModels()

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

        // Observe Created Tests with Attendees
        viewModel.createdTestsWithAttendees.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyCreatedHistory.visibility = View.VISIBLE
                binding.llCreatedTestsContainer.removeAllViews()
            } else {
                binding.tvEmptyCreatedHistory.visibility = View.GONE
                renderCreatedTests(list)
            }
        }

        // Observe Attended Tests
        viewModel.myAttempts.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyAttendedHistory.visibility = View.VISIBLE
                binding.llAttendedTestsContainer.removeAllViews()
            } else {
                binding.tvEmptyAttendedHistory.visibility = View.GONE
                renderAttendedTests(list)
            }
        }
    }

    private fun renderCreatedTests(list: List<TestViewModel.CreatedTestWithAttendees>) {
        val container = binding.llCreatedTestsContainer
        container.removeAllViews()
        val density = resources.displayMetrics.density

        list.forEach { item ->
            val test = item.test
            val card = CardView(requireContext()).apply {
                radius = 12 * density
                cardElevation = 2 * density
                setCardBackgroundColor(Color.parseColor("#F8FAFC"))
                useCompatPadding = true
            }

            val ll = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val p = (14 * density).toInt()
                setPadding(p, p, p, p)
            }

            // Header: Title & Delete
            val headerLl = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }

            val dateStr = test.createdAt?.time?.formatDate() ?: "Recently"

            val tvTitle = TextView(requireContext()).apply {
                text = "${test.title}"
                setTextColor(Color.parseColor("#1E293B"))
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnDelete = MaterialButton(requireContext()).apply {
                text = "🗑 Delete"
                textSize = 11f
                setTextColor(Color.parseColor("#EF4444"))
                setBackgroundColor(Color.parseColor("#FEE2E2"))
                val pB = (4 * density).toInt()
                setPadding(pB, pB, pB, pB)
                setOnClickListener {
                    viewModel.deleteTest(test.id)
                }
            }

            headerLl.addView(tvTitle)
            headerLl.addView(btnDelete)
            ll.addView(headerLl)

            // Subtitle: Skill & Difficulty & Date
            val tvMeta = TextView(requireContext()).apply {
                text = "Skill: ${test.skill} (${test.difficulty}) | ❓ ${item.questionCount} Qs\n📅 Created: $dateStr"
                setTextColor(Color.parseColor("#64748B"))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (10 * density).toInt() }
            }
            ll.addView(tvMeta)

            // Test Attendees Section
            val attendeesCard = CardView(requireContext()).apply {
                radius = 8 * density
                cardElevation = 1 * density
                setCardBackgroundColor(Color.WHITE)
            }

            val attendeesLl = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val pA = (10 * density).toInt()
                setPadding(pA, pA, pA, pA)
            }

            val tvAttendeesHeader = TextView(requireContext()).apply {
                text = "Test Attendees (${item.attendees.size})"
                setTextColor(Color.parseColor("#1565C0"))
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }
            attendeesLl.addView(tvAttendeesHeader)

            if (item.attendees.isEmpty()) {
                val tvNoAtt = TextView(requireContext()).apply {
                    text = "No users have attended this test yet."
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 12f
                }
                attendeesLl.addView(tvNoAtt)
            } else {
                item.attendees.forEach { att ->
                    val attDate = att.attemptedAt?.time?.formatDate() ?: "Recently"
                    val attTime = att.timeTakenStr.ifEmpty { if (att.timeTakenSec > 0) "${att.timeTakenSec}s" else "N/A" }
                    val attRow = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundColor(Color.parseColor("#F8FAFC"))
                        val pR = (8 * density).toInt()
                        setPadding(pR, pR, pR, pR)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = (4 * density).toInt() }
                    }

                    val tvAttName = TextView(requireContext()).apply {
                        text = "👤 ${att.userName}"
                        setTextColor(Color.parseColor("#1E293B"))
                        textSize = 13f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val tvAttStats = TextView(requireContext()).apply {
                        text = "Score : ${att.score}/${att.total} (${att.percentage}%)\nTime : $attTime\nDate : $attDate"
                        setTextColor(Color.parseColor("#475569"))
                        textSize = 12f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = (2 * density).toInt() }
                    }

                    attRow.addView(tvAttName)
                    attRow.addView(tvAttStats)
                    attendeesLl.addView(attRow)
                }
            }

            attendeesCard.addView(attendeesLl)
            ll.addView(attendeesCard)
            card.addView(ll)
            container.addView(card)
        }
    }

    private fun renderAttendedTests(list: List<TestAttempt>) {
        val container = binding.llAttendedTestsContainer
        container.removeAllViews()
        val density = resources.displayMetrics.density

        list.forEach { att ->
            val card = CardView(requireContext()).apply {
                radius = 12 * density
                cardElevation = 2 * density
                setCardBackgroundColor(Color.parseColor("#F0FDF4"))
                useCompatPadding = true
            }

            val ll = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val p = (14 * density).toInt()
                setPadding(p, p, p, p)
            }

            val attDate = att.attemptedAt?.time?.formatDate() ?: "Recently"
            val attTime = att.timeTakenStr.ifEmpty { if (att.timeTakenSec > 0) "${att.timeTakenSec}s" else "N/A" }

            val creatorStr = att.creatorName.ifEmpty { "Connection" }

            val tvTitle = TextView(requireContext()).apply {
                text = "${att.testTitle} (${att.percentage}% Accuracy)"
                setTextColor(Color.parseColor("#166534"))
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }

            val tvDetails = TextView(requireContext()).apply {
                text = "Skill: ${att.skill.ifEmpty { "General" }} (${att.difficulty.ifEmpty { "Basic" }})\n" +
                        "Creator: 👤 $creatorStr\n" +
                        "Score: ${att.score} / ${att.total} (${att.percentage}%)\n" +
                        "Correct: ${att.correctAnswers} ✅  |  Wrong: ${att.wrongAnswers} ❌\n" +
                        "Time Taken: ⏱️ $attTime | Date: 📅 $attDate"
                setTextColor(Color.parseColor("#334155"))
                textSize = 12.5f
                setLineSpacing(5f, 1f)
            }

            ll.addView(tvTitle)
            ll.addView(tvDetails)
            card.addView(ll)
            container.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
