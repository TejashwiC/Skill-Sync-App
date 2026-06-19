package com.skillsync.app.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.skillsync.app.data.model.Session
import java.util.concurrent.TimeUnit

object ReminderManager {

    fun scheduleSessionReminders(context: Context, session: Session) {
        if (!session.isScheduled) return

        val workManager = WorkManager.getInstance(context)
        val now = System.currentTimeMillis()

        val time24h = session.scheduledTime - TimeUnit.HOURS.toMillis(24)
        val time1h = session.scheduledTime - TimeUnit.HOURS.toMillis(1)
        val time15m = session.scheduledTime - TimeUnit.MINUTES.toMillis(15)

        if (time24h > now) {
            scheduleReminder(workManager, time24h - now, session.id, "Reminder: Your ${session.name} Session is tomorrow.", "Tap to view session details.")
        }
        if (time1h > now) {
            scheduleReminder(workManager, time1h - now, session.id, "Reminder: Your session starts in 1 hour.", "Get ready for ${session.name}.")
        }
        if (time15m > now) {
            scheduleReminder(workManager, time15m - now, session.id, "Reminder: Your session starts in 15 minutes.", "Join ${session.name} soon.")
        }
    }

    private fun scheduleReminder(workManager: WorkManager, delayMillis: Long, sessionId: String, title: String, message: String) {
        val data = Data.Builder()
            .putString("sessionId", sessionId)
            .putString("title", title)
            .putString("message", message)
            .build()

        val request = OneTimeWorkRequestBuilder<SessionReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }
}
