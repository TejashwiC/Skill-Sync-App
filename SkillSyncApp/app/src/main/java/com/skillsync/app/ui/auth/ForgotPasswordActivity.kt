package com.skillsync.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skillsync.app.data.repository.AuthRepository
import com.skillsync.app.databinding.ActivityForgotPasswordBinding
import com.skillsync.app.util.hideKeyboard
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReset.setOnClickListener {
            performPasswordReset()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performPasswordReset() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            showToast("Please enter email")
            return
        }

        hideKeyboard()
        binding.progressBar.show()
        binding.btnReset.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.sendPasswordReset(email)
            binding.progressBar.hide()
            binding.btnReset.isEnabled = true

            if (result.isSuccess) {
                showToast("Password reset email sent!")
                finish()
            } else {
                showToast("Reset Error: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
