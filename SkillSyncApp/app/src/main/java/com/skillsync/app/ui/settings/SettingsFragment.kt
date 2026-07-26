package com.skillsync.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

        // Observe action results
        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val msg = it.getOrNull()
                    if (msg == "ACCOUNT_DELETED") {
                        showToast("Account deleted successfully.")
                        com.skillsync.app.util.FirebaseUtil.auth.signOut()
                        val intent = android.content.Intent(requireContext(), com.skillsync.app.ui.auth.LoginActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        showToast(msg ?: "Action successful!")
                    }
                } else {
                    showToast("Error: " + (it.exceptionOrNull()?.message ?: "Operation failed"))
                }
                viewModel.resetActionResult()
            }
        }

        binding.cardBlockedUsers.setOnClickListener {
            findNavController().navigate(com.skillsync.app.R.id.nav_blocked_users)
        }

        binding.cardAccountSettings.setOnClickListener {
            showAccountSettingsDialog()
        }

        binding.cardPasswordSettings.setOnClickListener {
            showPasswordSettingsDialog()
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showAccountSettingsDialog() {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Account Settings")

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val nameInput = android.widget.EditText(context).apply {
            hint = "Display Name"
            setText(com.skillsync.app.util.FirebaseUtil.currentUser?.displayName ?: "")
        }

        val emailInput = android.widget.EditText(context).apply {
            hint = "Email (Read-only)"
            setText(com.skillsync.app.util.FirebaseUtil.currentUser?.email ?: "")
            isEnabled = false
        }

        layout.addView(nameInput)
        layout.addView(emailInput)
        builder.setView(layout)

        builder.setPositiveButton("Save Changes") { dialog, _ ->
            val newName = nameInput.text.toString().trim()
            if (newName.isEmpty()) {
                showToast("Please enter a valid name.")
            } else {
                viewModel.updateAccountName(newName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showPasswordSettingsDialog() {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Update Password")

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val newPassInput = android.widget.EditText(context).apply {
            hint = "New Password (min 6 chars)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val confirmPassInput = android.widget.EditText(context).apply {
            hint = "Confirm New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(newPassInput)
        layout.addView(confirmPassInput)
        builder.setView(layout)

        builder.setPositiveButton("Update Password") { dialog, _ ->
            val newPass = newPassInput.text.toString().trim()
            val confirmPass = confirmPassInput.text.toString().trim()
            if (newPass.length < 6) {
                showToast("Password must be at least 6 characters.")
            } else if (newPass != confirmPass) {
                showToast("Passwords do not match.")
            } else {
                viewModel.updatePassword(newPass)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showDeleteAccountDialog() {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Delete Account Permanently")
        builder.setMessage("Are you sure you want to permanently delete your account?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            viewModel.deleteAccount()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
