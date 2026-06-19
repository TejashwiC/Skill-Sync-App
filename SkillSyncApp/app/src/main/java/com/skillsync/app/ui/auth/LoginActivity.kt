package com.skillsync.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skillsync.app.data.repository.AuthRepository
import com.skillsync.app.databinding.ActivityLoginBinding
import com.skillsync.app.ui.main.MainActivity
import com.skillsync.app.util.hideKeyboard
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto login check
        if (authRepository.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            showToast("Please enter email")
            return
        }
        if (password.isEmpty()) {
            showToast("Please enter password")
            return
        }

        hideKeyboard()
        binding.progressBar.show()
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            binding.progressBar.hide()
            binding.btnLogin.isEnabled = true

            if (result.isSuccess) {
                showToast("Login Successful!")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                showToast("Login Error: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
