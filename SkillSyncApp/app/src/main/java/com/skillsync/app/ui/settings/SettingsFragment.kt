package com.skillsync.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.skillsync.app.databinding.FragmentSettingsBinding
import com.skillsync.app.util.showToast

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe settings values and update checkboxes
        viewModel.userSettings.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.switchFollowNotif.isChecked = it.followNotifications
                binding.switchSoundNotif.isChecked = it.soundNotifications
                binding.switchEmailNotif.isChecked = it.emailNotifications
                binding.switchSessionNotif.isChecked = it.sessionAlerts
            }
        }

        // Listener setup for each notification option
        binding.switchFollowNotif.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("followNotifications", isChecked)
            showToast("Follow notifications updated!")
        }

        binding.switchSoundNotif.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("soundNotifications", isChecked)
            showToast("Sound notifications updated!")
        }

        binding.switchEmailNotif.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("emailNotifications", isChecked)
            showToast("Email copies updated!")
        }

        binding.switchSessionNotif.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("sessionAlerts", isChecked)
            showToast("Session alerts updated!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
