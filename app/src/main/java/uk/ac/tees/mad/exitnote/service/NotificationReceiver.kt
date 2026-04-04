package uk.ac.tees.mad.exitnote.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALL_GOOD = "uk.ac.tees.mad.exitnote.ALL_GOOD"
        const val ACTION_SNOOZE = "uk.ac.tees.mad.exitnote.SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ALL_GOOD -> {
                NotificationHelper(context).cancelNotification()
                Log.d("NotificationReceiver", "User said all good")
            }
            ACTION_SNOOZE -> {
                NotificationHelper(context).cancelNotification()
                Log.d("NotificationReceiver", "Snoozing for 5 minutes")
            }
        }
    }
}