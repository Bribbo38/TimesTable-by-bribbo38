package com.selfhosttinker.timestable.ui.schedule

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.ui.components.ClassCard
import com.selfhosttinker.timestable.ui.components.GradientText
import com.selfhosttinker.timestable.ui.components.pulsating
import com.selfhosttinker.timestable.ui.theme.*

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun ScheduleScreen(
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
        WeekGridScreen(
            onNavigateToClassDetail = onNavigateToClassDetail
        )
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

            // Week picker — only if numberOfWeeks > 1
            if (settings.numberOfWeeks > 1) {
                WeekPicker(
                    numberOfWeeks = settings.numberOfWeeks,
                    selectedWeek  = selectedWeek,
                    onWeekSelected = viewModel::selectWeek,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Day picker — always scrollable so pills are never compressed
            DayPicker(
                showWeekends   = settings.showWeekends,
                selectedDay    = selectedDay,
                todayDay       = todayDay,
                onDaySelected  = viewModel::selectDay,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )

            if (classes.isEmpty()) {
                EmptyScheduleState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(classes, key = { it.id }) { schoolClass ->
                        ClassCard(
                            schoolClass = schoolClass,
                            onClick = { onNavigateToClassDetail(schoolClass.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekPicker(
    numberOfWeeks: Int,
    selectedWeek: Int,
    onWeekSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (w in 1..numberOfWeeks) {
            val isSelected = w == selectedWeek
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "weekScale$w"
            )
            Box(
                modifier = Modifier
                    .scale(scale)
                    .widthIn(min = 52.dp)
                    .clip(RoundedCornerShape(PillRadius))
                    .background(
                        if (isSelected) Brush.linearGradient(listOf(ElectricBlue, Indigo))
                        else Brush.linearGradient(listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        ))
                    )
                    .clickable { onWeekSelected(w) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "W$w",
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DayPicker(
    showWeekends: Boolean,
    selectedDay: Int,
    todayDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayCount = if (showWeekends) 7 else 5
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (d in 1..dayCount) {
            val isSelected = d == selectedDay
            val isToday    = d == todayDay
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "dayScale$d"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scale)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)         // fixed size — never compressed
                        .clip(RoundedCornerShape(ChipRadius))
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(ElectricBlue, Indigo))
                            else Brush.linearGradient(listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            ))
                        )
                        .clickable { onDaySelected(d) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DAY_LABELS[d - 1],
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                if (isToday) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(CoralRed)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyScheduleState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .pulsating(),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GradientText(
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
