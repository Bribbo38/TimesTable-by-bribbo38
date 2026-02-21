package com.selfhosttinker.timestable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.unit.ColorProvider
import com.selfhosttinker.timestable.MainActivity
import com.selfhosttinker.timestable.data.db.AppDatabase
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.components.formatTime
import kotlinx.coroutines.flow.first
import java.util.Calendar

class NowNextWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 80.dp),
            DpSize(220.dp, 80.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todayClasses = getTodayClasses(context)
        val nowMs = System.currentTimeMillis() % 86_400_000L
        val current = todayClasses.firstOrNull { it.startTimeMs <= nowMs && it.endTimeMs > nowMs }
        val next    = todayClasses.firstOrNull { it.startTimeMs > nowMs }

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
                    if (isSmall) {
                        SmallNowNextContent(current, next)
                    } else {
                        MediumNowNextContent(current, next)
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
        } catch (e: Exception) {
            emptyList()
        } finally {
            db.close()
        }
    }
}

@Composable
private fun SmallNowNextContent(current: SchoolClass?, next: SchoolClass?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        if (current != null) {
            val color = try {
                ColorProvider(Color(android.graphics.Color.parseColor(current.hexColor)))
            } catch (e: Exception) {
                GlanceTheme.colors.primary
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = GlanceModifier.width(4.dp).height(20.dp).background(color)) { }
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = current.name,
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "${formatTime(current.startTimeMs)}–${formatTime(current.endTimeMs)}",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        } else {
            Text(
                text = "Free now",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Medium)
            )
        }
        if (next != null) {
            Text(
                text = "Up next: ${next.name} ${formatTime(next.startTimeMs)}",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun MediumNowNextContent(current: SchoolClass?, next: SchoolClass?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        NowNextRow(label = "Current:", schoolClass = current, emptyText = "Free now")
        NowNextRow(label = "Up next:", schoolClass = next, emptyText = "No more classes")
    }
}

@Composable
private fun NowNextRow(label: String, schoolClass: SchoolClass?, emptyText: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontWeight = FontWeight.Medium),
            modifier = GlanceModifier.width(60.dp)
        )
        if (schoolClass != null) {
            val color = try {
                ColorProvider(Color(android.graphics.Color.parseColor(schoolClass.hexColor)))
            } catch (e: Exception) {
                GlanceTheme.colors.primary
            }
            Box(modifier = GlanceModifier.width(4.dp).height(20.dp).background(color)) { }
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "${schoolClass.name}  ${formatTime(schoolClass.startTimeMs)}–${formatTime(schoolClass.endTimeMs)}",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Normal)
            )
        } else {
            Text(
                text = emptyText,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}
