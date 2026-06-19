package com.skillsync.app

import android.app.Application
import com.google.firebase.FirebaseApp

class SkillSyncApp : Application() {
    companion object {
        lateinit var appContext: android.content.Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        
        // Initialize Firebase App
        FirebaseApp.initializeApp(this)
    }
}
