package com.skillsync.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.skillsync.app.R
import com.skillsync.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle the permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request Notification Permission for Android 13+
        askNotificationPermission()

        // Setup Jetpack Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Bind Custom Bottom Nav
        binding.tabHome.setOnClickListener { navController.navigate(R.id.nav_home) }
        binding.tabUsers.setOnClickListener { navController.navigate(R.id.nav_users) }
        binding.tabChat.setOnClickListener { navController.navigate(R.id.nav_chat) }
        binding.tabSession.setOnClickListener { navController.navigate(R.id.nav_session) }
        binding.tabTest.setOnClickListener { navController.navigate(R.id.nav_test) }
        binding.tabCalendar.setOnClickListener { navController.navigate(R.id.nav_calendar) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_chat_conversation) {
                binding.customBottomNav.visibility = android.view.View.GONE
            } else {
                binding.customBottomNav.visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        updateOnlineStatus(false)
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        val uid = com.skillsync.app.util.FirebaseUtil.currentUid
        if (uid.isNotEmpty()) {
            com.skillsync.app.util.FirebaseUtil.firestore.collection(com.skillsync.app.util.Constants.COLL_USERS)
                .document(uid)
                .update(
                    "isOnline", isOnline,
                    "lastSeen", System.currentTimeMillis()
                )
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show an educational UI here if needed, then request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
