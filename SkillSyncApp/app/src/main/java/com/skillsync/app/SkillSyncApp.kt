package com.skillsync.app

import android.app.Application
import com.google.firebase.FirebaseApp

class SkillSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase App
        FirebaseApp.initializeApp(this)
    }
}
