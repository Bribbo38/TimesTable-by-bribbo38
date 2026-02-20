package com.selfhosttinker.timestable.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.selfhosttinker.timestable.data.db.AppDatabase
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED") return

        val scheduler = NotificationScheduler()
        scheduler.createChannel(context)

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            // Re-schedule all class alarms after boot
            val db = AppDatabase::class.java.let {
                androidx.room.Room.databaseBuilder(context.applicationContext, it, "timestable.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            try {
                val classes = db.schoolClassDao().getAll()
                classes.collect { list ->
                    list.forEach { entity ->
                        scheduler.scheduleClass(
                            context,
                            com.selfhosttinker.timestable.domain.model.SchoolClass(
                                id = entity.id,
                                name = entity.name,
                                room = entity.room,
                                dayOfWeek = entity.dayOfWeek,
                                startTimeMs = entity.startTimeMs,
                                endTimeMs = entity.endTimeMs,
                                hexColor = entity.hexColor
                            )
                        )
                    }
                    // Only need the first emission
                    return@collect
                }
            } finally {
                db.close()
            }
        }
    }
}
