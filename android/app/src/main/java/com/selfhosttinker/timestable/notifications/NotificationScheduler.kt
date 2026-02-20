package com.selfhosttinker.timestable.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.selfhosttinker.timestable.domain.model.SchoolClass
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

const val EXTRA_CLASS_ID   = "class_id"
const val EXTRA_CLASS_NAME = "class_name"
const val EXTRA_CLASS_ROOM = "class_room"
const val CHANNEL_ID       = "class_reminders"

@Singleton
class NotificationScheduler @Inject constructor() {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Class Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders 15 minutes before each class"
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    fun scheduleClass(context: Context, schoolClass: SchoolClass) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextOccurrence(
            appDayOfWeek = schoolClass.dayOfWeek,
            classStartMs = schoolClass.startTimeMs,
            reminderOffsetMs = 15 * 60_000L
        )

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_CLASS_ID, schoolClass.id)
            putExtra(EXTRA_CLASS_NAME, schoolClass.name)
            putExtra(EXTRA_CLASS_ROOM, schoolClass.room ?: "")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schoolClass.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelClass(context: Context, classId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            classId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    /**
     * Converts app dayOfWeek (1=Mon…7=Sun) to Android Calendar day constant.
     */
    fun appDayToAndroid(dayOfWeek: Int): Int =
        if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1

    private fun nextOccurrence(appDayOfWeek: Int, classStartMs: Long, reminderOffsetMs: Long): Long {
        val cal = Calendar.getInstance()
        val targetAndroidDay = appDayToAndroid(appDayOfWeek)
        val classHour = ((classStartMs / 60_000) / 60).toInt() % 24
        val classMin  = ((classStartMs / 60_000) % 60).toInt()

        cal.set(Calendar.HOUR_OF_DAY, classHour)
        cal.set(Calendar.MINUTE, classMin)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val daysUntil = ((targetAndroidDay - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7).let {
            if (it == 0 && cal.timeInMillis - reminderOffsetMs < System.currentTimeMillis()) 7 else it
        }
        cal.add(Calendar.DAY_OF_MONTH, daysUntil)

        return cal.timeInMillis - reminderOffsetMs
    }
}
