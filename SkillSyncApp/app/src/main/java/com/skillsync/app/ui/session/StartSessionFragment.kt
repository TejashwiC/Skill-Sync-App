package com.skillsync.app.ui.session

import android.content.Intent
import android.net.Uri
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
        val platformAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platforms)
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPlatform.adapter = platformAdapter

        // Setup Duration Spinner
        val durations = listOf("30 minutes", "45 minutes", "1 hour", "1.5 hours", "2 hours", "3 hours")
        val durationMinsMap = mapOf("30 minutes" to 30L, "45 minutes" to 45L, "1 hour" to 60L, "1.5 hours" to 90L, "2 hours" to 120L, "3 hours" to 180L)
        val durationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, durations)
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDuration.adapter = durationAdapter
        binding.spDuration.setSelection(2) // Default: 1 hour

        // Step 1: Platform card buttons - open meeting creation page in browser
        binding.cardZoom.setOnClickListener {
            openUrl("https://zoom.us/start/videomeeting")
            // Pre-select Zoom in spinner
            val zoomIndex = Constants.PLATFORMS.indexOfFirst { it.id == "zoom" }
            if (zoomIndex >= 0) binding.spPlatform.setSelection(zoomIndex)
        }

        binding.cardGoogleMeet.setOnClickListener {
            openUrl("https://meet.google.com/new")
            // Pre-select Google Meet in spinner
            val meetIndex = Constants.PLATFORMS.indexOfFirst { it.id == "meet" }
            if (meetIndex >= 0) binding.spPlatform.setSelection(meetIndex)
        }

        // Schedule toggle
        binding.switchSchedule.setOnCheckedChangeListener { _, isChecked ->
            binding.llScheduleContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.btnStartSubmit.text = if (isChecked) "▶  Schedule Session" else "▶  Start Session & Get Code"
            if (isChecked && selectedCalendar == null) {
                selectedCalendar = Calendar.getInstance()
            }
        }

        binding.btnSelectDate.setOnClickListener {
            val c = selectedCalendar ?: Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                if (selectedCalendar == null) selectedCalendar = Calendar.getInstance()
                selectedCalendar?.set(year, month, day)
                binding.btnSelectDate.text = "$day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSelectTime.setOnClickListener {
            val c = selectedCalendar ?: Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                if (selectedCalendar == null) selectedCalendar = Calendar.getInstance()
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

            val platformId = Constants.PLATFORMS.find { it.label == platformLabel }?.id ?: "other"
            val durationMins = mapOf("30 minutes" to 30L, "45 minutes" to 45L, "1 hour" to 60L, "1.5 hours" to 90L, "2 hours" to 120L, "3 hours" to 180L)[binding.spDuration.selectedItem.toString()] ?: 60L

            if (name.isEmpty()) {
                showToast("Please enter a session name")
                return@setOnClickListener
            }
            if (meetingLink.isEmpty()) {
                showToast("Please paste your meeting link first")
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

            viewModel.startSession(name, skill, platformId, meetingLink, isScheduled, scheduledTime, durationMins)
        }

        viewModel.startResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val isScheduled = binding.switchSchedule.isChecked
                    val scheduledTime = if (isScheduled) selectedCalendar?.timeInMillis ?: 0L else 0L
                    if (isScheduled) {
                        showToast("Session Scheduled Successfully!")
                        val dummySession = com.skillsync.app.data.model.Session(
                            id = it.getOrNull() ?: "",
                            name = binding.etSessionName.text.toString().trim(),
                            isScheduled = true,
                            scheduledTime = scheduledTime
                        )
                        com.skillsync.app.worker.ReminderManager.scheduleSessionReminders(requireContext(), dummySession)
                    } else {
                        showToast("Session Launched! Your link is live for followers.")
                    }
                    viewModel.resetStartResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            showToast("Could not open browser: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
