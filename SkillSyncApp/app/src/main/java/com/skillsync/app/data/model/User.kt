package com.skillsync.app.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val teach: String = "",
    val learn: String = "",
    val language: String = "",
    val credits: Long = 100,
    val skills: List<String> = emptyList(),
    val photo: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val blocked: List<String> = emptyList(),
    val chatNotifications: Boolean = true,
    val followNotifications: Boolean = true,
    val soundNotifications: Boolean = true,
    val emailNotifications: Boolean = true,
    val sessionAlerts: Boolean = true,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val fcmToken: String = ""
)
