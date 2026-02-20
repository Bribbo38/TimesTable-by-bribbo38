package com.selfhosttinker.timestable

import android.app.Application
import com.selfhosttinker.timestable.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TimesTableApplication : Application() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()
        notificationScheduler.createChannel(this)
    }
}
