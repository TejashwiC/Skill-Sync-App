package com.skillsync.app.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtil {
    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUid: String
        get() = currentUser?.uid ?: ""
}
