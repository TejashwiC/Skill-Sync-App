package com.skillsync.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skillsync.app.data.model.User
import com.skillsync.app.data.repository.AuthRepository
import com.skillsync.app.data.repository.UserRepository
import com.skillsync.app.databinding.ActivityRegisterBinding
import com.skillsync.app.util.hideKeyboard
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            showToast("Please enter full name")
            return
        }
        if (email.isEmpty()) {
            showToast("Please enter email")
            return
        }
        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return
        }

        hideKeyboard()
        binding.progressBar.show()
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            val authResult = authRepository.register(email, password)
            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()?.user
                if (firebaseUser != null) {
                    val defaultUser = User(
                        uid = firebaseUser.uid,
                        name = name,
                        email = email,
                        mobile = "",
                        teach = "",
                        learn = "",
                        language = "",
                        credits = 100L
                    )
                    val dbResult = userRepository.createUserProfile(defaultUser)
                    binding.progressBar.hide()
                    binding.btnRegister.isEnabled = true

                    if (dbResult.isSuccess) {
                        showToast("Registration Successful!")
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Profile Creation Failed: ${dbResult.exceptionOrNull()?.message}")
                    }
                } else {
                    binding.progressBar.hide()
                    binding.btnRegister.isEnabled = true
                    showToast("User details empty after creation")
                }
            } else {
                binding.progressBar.hide()
                binding.btnRegister.isEnabled = true
                showToast("Registration Error: ${authResult.exceptionOrNull()?.message}")
            }
        }
    }
}
