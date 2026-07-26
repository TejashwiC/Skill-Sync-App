package com.skillsync.app.ui.calendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.databinding.FragmentCalendarBinding
import com.skillsync.app.util.FirebaseUtil
import com.skillsync.app.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter

    private var currentMonth = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarRecyclerView()
        updateCalendarUI()

        binding.btnPrevMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateCalendarUI()
        }

        binding.btnNextMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateCalendarUI()
        }

        viewModel.calendarSessions.observe(viewLifecycleOwner) { sessions ->
            calendarAdapter.setSessions(sessions)
            updateSessionsList(calendarAdapter.selectedDate)
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) showToast("Session deleted")
                else showToast("Delete failed: ${it.exceptionOrNull()?.message}")
                viewModel.resetDeleteResult()
            }
        }
    }

    private fun setupCalendarRecyclerView() {
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = CalendarAdapter { selectedDate ->
            updateSessionsList(selectedDate)
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            binding.tvSelectedDateHeader.text = "Sessions on ${sdf.format(selectedDate)}"
        }
        binding.rvCalendar.adapter = calendarAdapter
    }

    private fun updateCalendarUI() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = sdf.format(currentMonth.time)

        val daysInMonth = getDaysInMonthArray(currentMonth)
        calendarAdapter.setDays(daysInMonth, currentMonth.get(Calendar.MONTH), currentMonth.get(Calendar.YEAR))
    }

    private fun getDaysInMonthArray(monthCalendar: Calendar): List<Date?> {
        val daysInMonthArray = ArrayList<Date?>()
        val calendar = monthCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        for (i in 0 until dayOfWeek) daysInMonthArray.add(null)

        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDays) {
            val c = calendar.clone() as Calendar
            c.set(Calendar.DAY_OF_MONTH, i)
            daysInMonthArray.add(c.time)
        }
        return daysInMonthArray
    }

    private fun updateSessionsList(selectedDate: Date) {
        val sessions = viewModel.calendarSessions.value ?: emptyList()
        val myUid = FirebaseUtil.currentUid
        val c1 = Calendar.getInstance().apply { time = selectedDate }

        val filtered = sessions.filter { session ->
            if (session.status == "ended") return@filter false
            val sessionMs = if (session.isScheduled && session.scheduledTime > 0) session.scheduledTime else session.startTime
            val c2 = Calendar.getInstance().apply { timeInMillis = sessionMs }
            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
        }

        // Build session cards dynamically in a LinearLayout inside rvSessions container
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())

        // Build custom in-memory adapter-like view list
        val adapter = CalendarSessionAdapter(
            sessions = filtered,
            currentUid = myUid,
            onJoinClick = { session ->
                val now = System.currentTimeMillis()
                val startMs = if (session.isScheduled && session.scheduledTime > 0) session.scheduledTime else session.startTime
                val endMs = if (session.endTime > 0) session.endTime else (startMs + (session.durationMins.coerceAtLeast(60L) * 60000L))

                when {
                    now < startMs -> {
                        val timeStr = android.text.format.DateFormat.format("h:mm a", java.util.Date(startMs)).toString()
                        showToast("Session hasn't started yet. Starts at $timeStr")
                    }
                    now >= endMs -> {
                        showToast("Session has ended")
                    }
                    else -> openMeetingLink(session.meetingLink)
                }
            },
            onDeleteClick = { session ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Session")
                    .setMessage("Are you sure you want to delete \"${session.name}\"?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteSession(session.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvSessions.adapter = adapter

        if (filtered.isEmpty()) {
            binding.tvSelectedDateHeader.text = "No sessions scheduled on this day"
        }
    }

    private fun openMeetingLink(link: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (e: Exception) {
            showToast("Could not open meeting link: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
