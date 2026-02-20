package com.selfhosttinker.timestable.ui.addclass

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.ui.components.ClassCard
import com.selfhosttinker.timestable.ui.components.ColorCircleGrid
import com.selfhosttinker.timestable.ui.components.formatTime
import com.selfhosttinker.timestable.ui.theme.*
import com.selfhosttinker.timestable.domain.model.SchoolClass

private val DAY_OPTIONS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassScreen(
    classId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditClassViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isEditing = classId != null

    LaunchedEffect(classId) { viewModel.loadClass(classId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }

    val previewClass = SchoolClass(
        name = state.name.ifEmpty { "Class Name" },
        room = state.room.ifEmpty { null },
        teacher = state.teacher.ifEmpty { null },
        dayOfWeek = state.dayOfWeek,
        weekIndex = state.weekIndex,
        startTimeMs = state.startTimeMs,
        endTimeMs = state.endTimeMs,
        hexColor = state.hexColor
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Class" else "Add Class") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live preview card
            ClassCard(
                schoolClass = previewClass,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            // Presets horizontal scroll
            if (presets.isNotEmpty()) {
                Text("Presets", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val presetColor = preset.hexColor.toComposeColor()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(PillRadius))
                                .background(presetColor.copy(alpha = 0.2f))
                                .clickable { viewModel.applyPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset.name,
                                color = presetColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Subject name
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Subject Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Room
            OutlinedTextField(
                value = state.room,
                onValueChange = viewModel::updateRoom,
                label = { Text("Room") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Teacher
            OutlinedTextField(
                value = state.teacher,
                onValueChange = viewModel::updateTeacher,
                label = { Text("Teacher") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Day of week dropdown
            var dayExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = dayExpanded,
                onExpandedChange = { dayExpanded = !dayExpanded }
            ) {
                OutlinedTextField(
                    value = DAY_OPTIONS[state.dayOfWeek - 1],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Day") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dayExpanded,
                    onDismissRequest = { dayExpanded = false }
                ) {
                    DAY_OPTIONS.forEachIndexed { index, day ->
                        DropdownMenuItem(
                            text = { Text(day) },
                            onClick = {
                                viewModel.updateDayOfWeek(index + 1)
                                dayExpanded = false
                            }
                        )
                    }
                }
            }

            // Time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimePickerButton(
                    label = "Start Time",
                    timeMs = state.startTimeMs,
                    onTimePicked = viewModel::updateStartTime,
                    modifier = Modifier.weight(1f)
                )
                TimePickerButton(
                    label = "End Time",
                    timeMs = state.endTimeMs,
                    onTimePicked = viewModel::updateEndTime,
                    modifier = Modifier.weight(1f)
                )
            }

            // Color picker
            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorCircleGrid(
                selectedColor = state.hexColor.toComposeColor(),
                onColorSelected = { color ->
                    val hex = "#%06X".format(0xFFFFFF and color.hashCode())
                    // Serialize color properly
                    val argb = android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                    viewModel.updateColor("#%06X".format(argb and 0xFFFFFF))
                }
            )

            // Save button
            Button(
                onClick = viewModel::save,
                enabled = state.name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(PillRadius)
            ) {
                Text(
                    text = if (isEditing) "Update Class" else "Add Class",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TimePickerButton(
    label: String,
    timeMs: Long,
    onTimePicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hours = (timeMs / 3_600_000L).toInt()
    val minutes = ((timeMs % 3_600_000L) / 60_000L).toInt()

    OutlinedButton(
        onClick = {
            TimePickerDialog(context, { _, h, m ->
                onTimePicked(h * 3_600_000L + m * 60_000L)
            }, hours, minutes, true).show()
        },
        modifier = modifier,
        shape = RoundedCornerShape(ChipRadius)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = formatTime(timeMs),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
