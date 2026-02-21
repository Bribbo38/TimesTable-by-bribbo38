package com.selfhosttinker.timestable.ui.spdesign

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.components.formatTime
import com.selfhosttinker.timestable.ui.schedule.ScheduleViewModel
import com.selfhosttinker.timestable.ui.schedule.WeekGridScreen
import com.selfhosttinker.timestable.ui.theme.CoralRed
import com.selfhosttinker.timestable.ui.theme.SPCardElevation
import com.selfhosttinker.timestable.ui.theme.SPCardRadius
import com.selfhosttinker.timestable.ui.theme.toComposeColor
import java.text.SimpleDateFormat
import java.util.*

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val FULL_DAY_LABELS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val MONTH_FORMAT = SimpleDateFormat("d MMM", Locale.getDefault())

@Composable
fun SPScheduleScreen(
    onNavigateToAddClass: (Int) -> Unit,
    onNavigateToClassDetail: (String) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val settings     by viewModel.settings.collectAsStateWithLifecycle()
    val selectedDay  by viewModel.selectedDay.collectAsStateWithLifecycle()
    val selectedWeek by viewModel.selectedWeek.collectAsStateWithLifecycle()
    val classes      by viewModel.classesForDay.collectAsStateWithLifecycle()
    val todayDay = remember { ScheduleViewModel.todayAppDay() }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        WeekGridScreen(onNavigateToClassDetail = onNavigateToClassDetail)
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddClass(selectedDay) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add class")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Week picker chips
            if (settings.numberOfWeeks > 1 && settings.repeatingWeeksEnabled) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (w in 1..settings.numberOfWeeks) {
                        FilterChip(
                            selected = w == selectedWeek,
                            onClick = { viewModel.selectWeek(w) },
                            label = { Text("Week $w") }
                        )
                    }
                }
            }

            // Day strip (FilterChips)
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val dayCount = if (settings.showWeekends) 7 else 5
                for (d in 1..dayCount) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilterChip(
                            selected = d == selectedDay,
                            onClick = { viewModel.selectDay(d) },
                            label = { Text(DAY_LABELS[d - 1], fontSize = 13.sp) }
                        )
                        if (d == todayDay) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(CoralRed)
                            )
                        }
                    }
                }
            }

            // Current day label
            val today = Calendar.getInstance()
            val dayLabel = buildString {
                append(FULL_DAY_LABELS.getOrNull(selectedDay - 1) ?: "")
                if (selectedDay == todayDay) {
                    append(", ")
                    append(MONTH_FORMAT.format(today.time))
                }
            }
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))

            if (classes.isEmpty()) {
                SPEmptyScheduleState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(classes, key = { it.id }) { schoolClass ->
                        SPClassCard(
                            schoolClass = schoolClass,
                            onClick = { onNavigateToClassDetail(schoolClass.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SPClassCard(
    schoolClass: SchoolClass,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classColor = schoolClass.hexColor.toComposeColor()

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = SPCardElevation),
        shape = RoundedCornerShape(SPCardRadius)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left border strip (4dp)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(classColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = schoolClass.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${formatTime(schoolClass.startTimeMs)}–${formatTime(schoolClass.endTimeMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val sub = listOfNotNull(schoolClass.room, schoolClass.teacher).joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SPEmptyScheduleState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Classes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first class",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
