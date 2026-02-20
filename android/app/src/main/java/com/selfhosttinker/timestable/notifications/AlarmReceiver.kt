package com.selfhosttinker.timestable.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.selfhosttinker.timestable.MainActivity
import com.selfhosttinker.timestable.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val classId   = intent.getStringExtra(EXTRA_CLASS_ID)   ?: return
        val className = intent.getStringExtra(EXTRA_CLASS_NAME) ?: return
        val classRoom = intent.getStringExtra(EXTRA_CLASS_ROOM)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (!classRoom.isNullOrBlank()) "Room: $classRoom · Starting in 15 minutes"
                   else "Starting in 15 minutes"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(className)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(classId.hashCode(), notification)
    }
}
