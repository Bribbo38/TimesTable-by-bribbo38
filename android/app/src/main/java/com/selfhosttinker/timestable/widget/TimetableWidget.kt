package com.selfhosttinker.timestable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.selfhosttinker.timestable.MainActivity
import com.selfhosttinker.timestable.data.db.AppDatabase
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.components.formatTime
import kotlinx.coroutines.flow.first
import java.util.Calendar

class TimetableWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),  // small
            DpSize(220.dp, 110.dp)   // medium
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todayClasses = getTodayClasses(context)

        provideContent {
            val size = LocalSize.current
            val isSmall = size.width < 200.dp

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .appWidgetBackground()
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    if (todayClasses.isEmpty()) {
                        Text(
                            text = "No classes today",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else if (isSmall) {
                        SmallWidgetContent(todayClasses.first())
                    } else {
                        MediumWidgetContent(todayClasses)
                    }
                }
            }
        }
    }

    private suspend fun getTodayClasses(context: Context): List<SchoolClass> {
        val db = AppDatabase::class.java.let {
            androidx.room.Room.databaseBuilder(context.applicationContext, it, "timestable.db")
                .fallbackToDestructiveMigration()
                .build()
        }
        return try {
            val todayAndroid = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val todayApp = if (todayAndroid == Calendar.SUNDAY) 7 else todayAndroid - 1
            db.schoolClassDao().getAll().first()
                .filter { it.dayOfWeek == todayApp }
                .sortedBy { it.startTimeMs }
                .map { entity ->
                    SchoolClass(
                        id = entity.id,
                        name = entity.name,
                        room = entity.room,
                        teacher = entity.teacher,
                        dayOfWeek = entity.dayOfWeek,
                        startTimeMs = entity.startTimeMs,
                        endTimeMs = entity.endTimeMs,
                        hexColor = entity.hexColor
                    )
                }
        } finally {
            db.close()
        }
    }
}

@Composable
private fun SmallWidgetContent(schoolClass: SchoolClass) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = schoolClass.name,
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
        Text(text = formatTime(schoolClass.startTimeMs))
        schoolClass.room?.let { room ->
            Text(text = room)
        }
    }
}

@Composable
private fun MediumWidgetContent(classes: List<SchoolClass>) {
    val visible = classes.take(4)
    val more = classes.size - visible.size

    Column(modifier = GlanceModifier.fillMaxSize()) {
        visible.forEach { schoolClass ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(schoolClass.startTimeMs),
                    style = TextStyle(fontWeight = FontWeight.Medium),
                    modifier = GlanceModifier.width(50.dp)
                )
                Text(
                    text = schoolClass.name,
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            }
        }
        if (more > 0) {
            Text(text = "+$more more")
        }
    }
}
