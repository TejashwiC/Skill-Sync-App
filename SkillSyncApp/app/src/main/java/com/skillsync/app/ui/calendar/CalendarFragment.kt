package com.skillsync.app.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.skillsync.app.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var sessionsAdapter: com.skillsync.app.ui.adapter.SessionAdapter

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
        setupSessionsRecyclerView()

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

    private fun setupSessionsRecyclerView() {
        sessionsAdapter = com.skillsync.app.ui.adapter.SessionAdapter(
            onJoinClick = { /* Handle if live */ },
            onEndClick = { /* Handle if host */ }
        )
        binding.rvSessions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvSessions.adapter = sessionsAdapter
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
        for (i in 0 until dayOfWeek) {
            daysInMonthArray.add(null)
        }

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
        val c1 = Calendar.getInstance()
        c1.time = selectedDate
        val filtered = sessions.filter { session ->
            val c2 = Calendar.getInstance()
            c2.timeInMillis = session.startTime
            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
        }
        sessionsAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
