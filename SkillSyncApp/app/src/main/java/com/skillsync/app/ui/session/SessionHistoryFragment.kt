package com.skillsync.app.ui.session

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
import com.skillsync.app.data.model.Session
import com.skillsync.app.databinding.FragmentSessionHistoryBinding
import com.skillsync.app.util.formatDate
import com.skillsync.app.util.formatTime

class SessionHistoryFragment : Fragment() {

    private var _binding: FragmentSessionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private var isCreatedTabSelected = true

    private var createdList: List<Session> = emptyList()
    private var attendedList: List<Session> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Tab Toggle Listeners
        binding.btnTabCreated.setOnClickListener {
            selectCreatedTab()
        }

        binding.btnTabAttended.setOnClickListener {
            selectAttendedTab()
        }

        // Observe Created Sessions
        viewModel.createdEndedSessions.observe(viewLifecycleOwner) { list ->
            createdList = list
            if (isCreatedTabSelected) {
                renderList(createdList, isCreatedTab = true)
            }
        }

        // Observe Attended Sessions
        viewModel.attendedEndedSessions.observe(viewLifecycleOwner) { list ->
            attendedList = list
            if (!isCreatedTabSelected) {
                renderList(attendedList, isCreatedTab = false)
            }
        }
    }

    private fun selectCreatedTab() {
        isCreatedTabSelected = true
        binding.btnTabCreated.setBackgroundColor(Color.WHITE)
        binding.btnTabCreated.setTextColor(Color.parseColor("#1E293B"))
        binding.btnTabCreated.elevation = 4f

        binding.btnTabAttended.setBackgroundColor(Color.TRANSPARENT)
        binding.btnTabAttended.setTextColor(Color.parseColor("#64748B"))
        binding.btnTabAttended.elevation = 0f

        renderList(createdList, isCreatedTab = true)
    }

    private fun selectAttendedTab() {
        isCreatedTabSelected = false
        binding.btnTabAttended.setBackgroundColor(Color.WHITE)
        binding.btnTabAttended.setTextColor(Color.parseColor("#1E293B"))
        binding.btnTabAttended.elevation = 4f

        binding.btnTabCreated.setBackgroundColor(Color.TRANSPARENT)
        binding.btnTabCreated.setTextColor(Color.parseColor("#64748B"))
        binding.btnTabCreated.elevation = 0f

        renderList(attendedList, isCreatedTab = false)
    }

    private fun renderList(sessions: List<Session>, isCreatedTab: Boolean) {
        val density = resources.displayMetrics.density

        if (sessions.isEmpty()) {
            binding.tvEmptyHistory.visibility = View.VISIBLE
            binding.tvEmptyHistory.text = if (isCreatedTab) {
                "No completed sessions created by you yet."
            } else {
                "No completed sessions attended by you yet."
            }
            binding.rvSessionHistory.visibility = View.GONE
            return
        }

        binding.tvEmptyHistory.visibility = View.GONE
        binding.rvSessionHistory.visibility = View.VISIBLE

        // We can render custom cards directly inside a vertical LinearLayout or custom RecyclerView Adapter
        // Here we build a dynamic vertical list for clean, precise spec alignment
        val parent = binding.rvSessionHistory
        parent.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<SessionViewHolder>() {
            override fun onCreateViewHolder(parentGroup: ViewGroup, viewType: Int): SessionViewHolder {
                val card = CardView(requireContext()).apply {
                    radius = 12 * density
                    cardElevation = 2 * density
                    setCardBackgroundColor(Color.WHITE)
                    useCompatPadding = true
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                return SessionViewHolder(card)
            }

            override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
                val session = sessions[position]
                holder.bind(session, isCreatedTab, density)
            }

            override fun getItemCount(): Int = sessions.size
        }
        parent.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    class SessionViewHolder(private val cardView: CardView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(cardView) {
        fun bind(session: Session, isCreatedTab: Boolean, density: Float) {
            cardView.removeAllViews()

            val mainLl = LinearLayout(cardView.context).apply {
                orientation = LinearLayout.VERTICAL
                val p = (14 * density).toInt()
                setPadding(p, p, p, p)
            }

            // Header row: Title & Completed Status Badge
            val headerLl = LinearLayout(cardView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (8 * density).toInt() }
            }

            val tvTitle = TextView(cardView.context).apply {
                text = session.name
                setTextColor(Color.parseColor("#1E293B"))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvStatus = TextView(cardView.context).apply {
                text = "Completed"
                setTextColor(Color.parseColor("#166534"))
                setBackgroundColor(Color.parseColor("#DCFCE7"))
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                val pH = (8 * density).toInt()
                val pV = (3 * density).toInt()
                setPadding(pH, pV, pH, pV)
            }

            headerLl.addView(tvTitle)
            headerLl.addView(tvStatus)
            mainLl.addView(headerLl)

            // Skill Name & Optional Host Name (for Attended Sessions)
            val tvSkill = TextView(cardView.context).apply {
                text = "Skill: ${session.skill}"
                setTextColor(Color.parseColor("#2563EB"))
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (4 * density).toInt() }
            }
            mainLl.addView(tvSkill)

            if (!isCreatedTab) {
                val tvHost = TextView(cardView.context).apply {
                    text = "Hosted by: Rahul".replace("Rahul", session.hostName.ifEmpty { "Host" })
                    setTextColor(Color.parseColor("#475569"))
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = (4 * density).toInt() }
                }
                mainLl.addView(tvHost)
            }

            val dateStr = if (session.startTime > 0) session.startTime.formatDate() else "26-07-2026"
            val startTimeStr = if (session.startTime > 0) session.startTime.formatTime() else "N/A"
            val endTimeCalc = if (session.endTime > 0) session.endTime else (session.startTime + (session.durationMins * 60000L))
            val endTimeStr = if (endTimeCalc > 0) endTimeCalc.formatTime() else "N/A"
            val durationText = if (session.durationMins > 0) "${session.durationMins} Minutes" else "45 Minutes"

            // Details Block
            val tvDetails = TextView(cardView.context).apply {
                val detailsText = if (isCreatedTab) {
                    "📅 Date: $dateStr\n" +
                    "⏰ Start Time: $startTimeStr  |  ⏱️ End Time: $endTimeStr\n" +
                    "⌛ Duration: $durationText  |  👤 Attendees: ${session.participants.size}"
                } else {
                    "📅 Date: $dateStr\n" +
                    "⏰ Start Time: $startTimeStr  |  ⏱️ End Time: $endTimeStr\n" +
                    "⌛ Duration: $durationText"
                }
                text = detailsText
                setTextColor(Color.parseColor("#334155"))
                textSize = 12.5f
                setLineSpacing(4f, 1f)
            }
            mainLl.addView(tvDetails)

            cardView.addView(mainLl)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
