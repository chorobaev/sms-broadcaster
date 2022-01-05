package io.flaterlab.smsbroadcast

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmsApp : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "SMS_BROADCASTER"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_id),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService<NotificationManager>()!!
            .createNotificationChannel(channel)
    }
}