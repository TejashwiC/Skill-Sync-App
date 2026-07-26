package com.skillsync.app.util

object Constants {
    // Firestore Collection Names
    const val COLL_USERS = "users"
    const val COLL_CHATS = "chats"
    const val COLL_MESSAGES = "messages"
    const val COLL_SESSIONS = "sessions"
    const val COLL_TESTS = "tests"
    const val COLL_QUESTIONS = "questions"
    const val COLL_TEST_ATTEMPTS = "testAttempts"
    const val COLL_PDFS = "pdfs"
    const val COLL_GROUPS = "groups"

    // Supported Meeting Platforms
    val PLATFORMS = listOf(
        Platform("zoom", "Zoom", "zoommtg://zoom.us/join"),
        Platform("meet", "Google Meet", "https://meet.google.com")
    )

    data class Platform(val id: String, val label: String, val deepLinkPrefix: String)
}
