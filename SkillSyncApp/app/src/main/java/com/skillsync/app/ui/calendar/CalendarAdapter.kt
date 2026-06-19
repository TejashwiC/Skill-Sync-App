package com.skillsync.app.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.Session
import com.skillsync.app.databinding.ItemCalendarDayBinding
import java.util.Calendar
import java.util.Date

class CalendarAdapter(private val onDateSelected: (Date) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = ArrayList<Date?>()
    private var currentMonth = 0
    private var currentYear = 0
    private var sessions = listOf<Session>()
    var selectedDate: Date = Date()
        private set

    fun setDays(days: List<Date?>, month: Int, year: Int) {
        this.days.clear()
        this.days.addAll(days)
        this.currentMonth = month
        this.currentYear = year
        
        // Find if selectedDate is in this month, else select today or 1st day
        val c = Calendar.getInstance()
        c.time = selectedDate
        if (c.get(Calendar.MONTH) != month || c.get(Calendar.YEAR) != year) {
            val today = Calendar.getInstance()
            if (today.get(Calendar.MONTH) == month && today.get(Calendar.YEAR) == year) {
                selectedDate = today.time
            } else {
                val firstDay = Calendar.getInstance()
                firstDay.set(year, month, 1)
                selectedDate = firstDay.time
            }
        }
        notifyDataSetChanged()
    }

    fun setSessions(sessions: List<Session>) {
        this.sessions = sessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        if (date == null) {
            holder.binding.tvDayNumber.text = ""
            holder.binding.vDot.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
            holder.binding.tvDayNumber.setBackgroundResource(0)
        } else {
            val c = Calendar.getInstance()
            c.time = date
            holder.binding.tvDayNumber.text = c.get(Calendar.DAY_OF_MONTH).toString()

            // Highlight if selected
            val selC = Calendar.getInstance()
            selC.time = selectedDate
            if (c.get(Calendar.DAY_OF_YEAR) == selC.get(Calendar.DAY_OF_YEAR) &&
                c.get(Calendar.YEAR) == selC.get(Calendar.YEAR)) {
                holder.binding.tvDayNumber.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.binding.tvDayNumber.setBackgroundResource(0)
            }

            // Check for sessions
            val hasSession = sessions.any { s ->
                val sc = Calendar.getInstance()
                sc.timeInMillis = s.startTime
                sc.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR) &&
                sc.get(Calendar.YEAR) == c.get(Calendar.YEAR)
            }
            holder.binding.vDot.visibility = if (hasSession) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                selectedDate = date
                notifyDataSetChanged()
                onDateSelected(date)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    class CalendarViewHolder(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)
}
