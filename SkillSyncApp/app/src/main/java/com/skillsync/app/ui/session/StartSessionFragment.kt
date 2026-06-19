package com.skillsync.app.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentStartSessionBinding
import com.skillsync.app.util.Constants
import com.skillsync.app.util.showToast

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar

class StartSessionFragment : Fragment() {

    private var _binding: FragmentStartSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()

    private var selectedCalendar: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Platform Spinner
        val platforms = Constants.PLATFORMS.map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPlatform.adapter = adapter

        binding.switchSchedule.setOnCheckedChangeListener { _, isChecked ->
            binding.llScheduleContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.btnStartSubmit.text = if (isChecked) "Schedule Session" else "Launch Live Session"
            if (isChecked && selectedCalendar == null) {
                selectedCalendar = Calendar.getInstance()
            }
        }

        binding.btnSelectDate.setOnClickListener {
            val c = selectedCalendar ?: Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                selectedCalendar?.set(year, month, day)
                binding.btnSelectDate.text = "$day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSelectTime.setOnClickListener {
            val c = selectedCalendar ?: Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                selectedCalendar?.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar?.set(Calendar.MINUTE, minute)
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val minStr = if (minute < 10) "0$minute" else "$minute"
                binding.btnSelectTime.text = "$hour:$minStr $amPm"
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }

        binding.btnStartSubmit.setOnClickListener {
            val name = binding.etSessionName.text.toString().trim()
            val skill = binding.etSessionSkill.text.toString().trim()
            val platformLabel = binding.spPlatform.selectedItem.toString()
            val meetingLink = binding.etSessionLink.text.toString().trim()

            // Find platform ID
            val platformId = Constants.PLATFORMS.find { it.label == platformLabel }?.id ?: "other"

            if (name.isEmpty() || meetingLink.isEmpty()) {
                showToast("Session name and meeting link are required")
                return@setOnClickListener
            }
            if (!meetingLink.startsWith("http")) {
                showToast("Meeting link must start with http:// or https://")
                return@setOnClickListener
            }

            val isScheduled = binding.switchSchedule.isChecked
            val scheduledTime = if (isScheduled) selectedCalendar?.timeInMillis ?: 0L else 0L

            if (isScheduled && scheduledTime < System.currentTimeMillis()) {
                showToast("Please select a future date and time for scheduled sessions")
                return@setOnClickListener
            }

            viewModel.startSession(name, skill, platformId, meetingLink, isScheduled, scheduledTime)
            
            // Note: We'll observe startResult to schedule local reminders
        }

        viewModel.startResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Session Launched Successfully!")
                    
                    // We need the full session object to schedule. But wait, startSession doesn't return the Session object.
                    // Let's modify startSession to return the Session or we can just fetch it.
                    // Wait, ReminderManager needs Session object. But it only uses id, isScheduled, name, scheduledTime.
                    // We can just construct a dummy session object for ReminderManager here.
                    val isScheduled = binding.switchSchedule.isChecked
                    val scheduledTime = if (isScheduled) selectedCalendar?.timeInMillis ?: 0L else 0L
                    if (isScheduled) {
                        val dummySession = com.skillsync.app.data.model.Session(
                            id = it.getOrNull() ?: "",
                            name = binding.etSessionName.text.toString().trim(),
                            isScheduled = true,
                            scheduledTime = scheduledTime
                        )
                        com.skillsync.app.worker.ReminderManager.scheduleSessionReminders(requireContext(), dummySession)
                    }

                    viewModel.resetStartResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
