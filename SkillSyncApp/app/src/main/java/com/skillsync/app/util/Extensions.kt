package com.skillsync.app.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Toast Extension
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun androidx.fragment.app.Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

// View Visibility Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Keyboard Helper Extension
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// Date/Time Formatting Extensions
fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    val mins = diff / 60000
    val hrs = diff / 3600000
    val days = diff / 86400000
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "$mins min ago"
        hrs < 24 -> "$hrs hr ago"
        else -> "$days day ago"
    }
}
