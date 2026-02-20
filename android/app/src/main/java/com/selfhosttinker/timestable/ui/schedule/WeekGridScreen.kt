package com.selfhosttinker.timestable.ui.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.components.formatTime
import com.selfhosttinker.timestable.ui.theme.CoralRed
import com.selfhosttinker.timestable.ui.theme.ElectricBlue
import com.selfhosttinker.timestable.ui.theme.toComposeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

private val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val HOUR_START = 7   // 07:00
private const val HOUR_END   = 22  // 22:00
private val HOUR_HEIGHT      = 60.dp
private val TIME_COL_WIDTH   = 48.dp
private val DAY_COL_WIDTH    = 130.dp

@HiltViewModel
class WeekGridViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {
    val allClasses: StateFlow<List<SchoolClass>> = classRepository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun WeekGridScreen(
    onNavigateToClassDetail: (String) -> Unit,
    viewModel: WeekGridViewModel = hiltViewModel()
) {
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
    val todayDay = remember { ScheduleViewModel.todayAppDay() }

    val verticalScroll = rememberScrollState(initial = 7 * 60) // start scrolled to 7am
    val horizontalScroll = rememberScrollState()
    val density = LocalDensity.current
    val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
    val dayColWidthPx = with(density) { DAY_COL_WIDTH.toPx() }
    val timeColWidthPx = with(density) { TIME_COL_WIDTH.toPx() }

    // Current time indicator position
    val currentTimeOffset = remember {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val min = now.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + min - HOUR_START * 60
        totalMinutes.coerceAtLeast(0) * hourHeightPx / 60f
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header row: day names
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            Spacer(modifier = Modifier.width(TIME_COL_WIDTH))
            for (day in 1..5) {
                Box(
                    modifier = Modifier
                        .width(DAY_COL_WIDTH)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = DAY_NAMES[day - 1],
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (day == todayDay) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Grid body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                // Time column
                Column(modifier = Modifier.width(TIME_COL_WIDTH)) {
                    for (hour in HOUR_START until HOUR_END) {
                        Box(
                            modifier = Modifier
                                .height(HOUR_HEIGHT)
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Text(
                                text = "%02d:00".format(hour),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Day columns
                for (day in 1..5) {
                    val dayClasses = allClasses.filter { it.dayOfWeek == day }
                    Box(
                        modifier = Modifier
                            .width(DAY_COL_WIDTH)
                            .height(HOUR_HEIGHT * (HOUR_END - HOUR_START))
                    ) {
                        // Hour lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (hour in 0..(HOUR_END - HOUR_START)) {
                                val y = hour * hourHeightPx
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 0.5.dp.toPx()
                                )
                            }
                        }

                        // Class blocks
                        dayClasses.forEach { schoolClass ->
                            val startMin = schoolClass.startTimeMs.toInt() / 60_000 - HOUR_START * 60
                            val endMin = schoolClass.endTimeMs.toInt() / 60_000 - HOUR_START * 60
                            val topDp = (startMin * HOUR_HEIGHT.value / 60f).dp
                            val heightDp = ((endMin - startMin) * HOUR_HEIGHT.value / 60f).dp.coerceAtLeast(30.dp)
                            val classColor = schoolClass.hexColor.toComposeColor()

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 1.dp)
                                    .offset(y = topDp)
                                    .fillMaxWidth()
                                    .height(heightDp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(classColor.copy(alpha = 0.85f))
                                    .clickable { onNavigateToClassDetail(schoolClass.id) }
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text(
                                        text = schoolClass.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = formatTime(schoolClass.startTimeMs),
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Current time indicator (today only)
                        if (day == todayDay) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = CoralRed,
                                    start = Offset(0f, currentTimeOffset),
                                    end = Offset(size.width, currentTimeOffset),
                                    strokeWidth = 2.dp.toPx()
                                )
                                drawCircle(
                                    color = CoralRed,
                                    radius = 4.dp.toPx(),
                                    center = Offset(0f, currentTimeOffset)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
