package com.selfhosttinker.timestable.ui.addclass

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.components.ClassCard
import com.selfhosttinker.timestable.ui.components.ColorCircleGrid
import com.selfhosttinker.timestable.ui.components.formatTime
import com.selfhosttinker.timestable.ui.theme.*

private val DAY_OPTIONS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassScreen(
    classId: String?,
    initialDay: Int = 0,
    onNavigateBack: () -> Unit,
    viewModel: AddEditClassViewModel = hiltViewModel()
) {
    val state              by viewModel.state.collectAsStateWithLifecycle()
    val presets            by viewModel.presets.collectAsStateWithLifecycle()
    val overlappingClasses by viewModel.overlappingClasses.collectAsStateWithLifecycle()
    val isEditing = classId != null
    val hasTimeError = state.startTimeMs >= state.endTimeMs

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker   by remember { mutableStateOf(false) }

    LaunchedEffect(classId)       { viewModel.loadClass(classId) }
    LaunchedEffect(initialDay)    { viewModel.initDay(initialDay) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }

    if (overlappingClasses.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearOverlapWarning() },
            title = { Text("Schedule Conflict") },
            text = {
                Column {
                    Text("This class overlaps with:")
                    Spacer(Modifier.height(8.dp))
                    overlappingClasses.forEach { conflict ->
                        Text("• ${conflict.name} (${formatTime(conflict.startTimeMs)}–${formatTime(conflict.endTimeMs)})")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearOverlapWarning()
                    viewModel.saveForce()
                }) { Text("Add Anyway") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearOverlapWarning() }) { Text("Go Back") }
            }
        )
    }

    val previewClass = SchoolClass(
        name        = state.name.ifEmpty { "Class Name" },
        room        = state.room.ifEmpty { null },
        teacher     = state.teacher.ifEmpty { null },
        dayOfWeek   = state.dayOfWeek,
        weekIndex   = state.weekIndex,
        startTimeMs = state.startTimeMs,
        endTimeMs   = state.endTimeMs,
        hexColor    = state.hexColor
    )

    // Material3 time picker dialogs
    if (showStartTimePicker) {
        ComposeTimePickerDialog(
            initialMs  = state.startTimeMs,
            onDismiss  = { showStartTimePicker = false },
            onConfirm  = { ms -> viewModel.updateStartTime(ms); showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        ComposeTimePickerDialog(
            initialMs  = state.endTimeMs,
            onDismiss  = { showEndTimePicker = false },
            onConfirm  = { ms -> viewModel.updateEndTime(ms); showEndTimePicker = false }
        )
    }

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
            // Live preview
            ClassCard(schoolClass = previewClass, onClick = {}, modifier = Modifier.fillMaxWidth())

            // Preset chips
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
                            Text(preset.name, color = presetColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Text fields
            OutlinedTextField(
                value = state.name, onValueChange = viewModel::updateName,
                label = { Text("Subject Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = state.room, onValueChange = viewModel::updateRoom,
                label = { Text("Room") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = state.teacher, onValueChange = viewModel::updateTeacher,
                label = { Text("Teacher") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = state.notes, onValueChange = viewModel::updateNotes,
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4
            )

            // Day picker dropdown
            var dayExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                OutlinedTextField(
                    value = DAY_OPTIONS[state.dayOfWeek - 1],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Day") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                    DAY_OPTIONS.forEachIndexed { index, day ->
                        DropdownMenuItem(
                            text = { Text(day) },
                            onClick = { viewModel.updateDayOfWeek(index + 1); dayExpanded = false }
                        )
                    }
                }
            }

            // Time pickers — Material3 clock UI
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeButton(
                    label = "Start Time", timeMs = state.startTimeMs,
                    onClick = { showStartTimePicker = true }, modifier = Modifier.weight(1f)
                )
                TimeButton(
                    label = "End Time", timeMs = state.endTimeMs,
                    onClick = { showEndTimePicker = true }, modifier = Modifier.weight(1f)
                )
            }
            if (hasTimeError) {
                Text(
                    text = "End time must be after start time",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Color picker
            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorCircleGrid(
                selectedColor = state.hexColor.toComposeColor(),
                onColorSelected = { color ->
                    val argb = android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red   * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue  * 255).toInt()
                    )
                    viewModel.updateColor("#%06X".format(argb and 0xFFFFFF))
                }
            )

            // Save button
            Button(
                onClick = viewModel::save,
                enabled = state.name.isNotBlank() && !hasTimeError,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(PillRadius)
            ) {
                Text(
                    text = if (isEditing) "Update Class" else "Add Class",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Material3 time picker dialog (replaces system TimePickerDialog) ─────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTimePickerDialog(
    initialMs: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initialHour   = (initialMs / 3_600_000L).toInt().coerceIn(0, 23)
    val initialMinute = ((initialMs % 3_600_000L) / 60_000L).toInt().coerceIn(0, 59)
    val state = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text  = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 3_600_000L + state.minute * 60_000L) }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TimeButton(
    label: String,
    timeMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(ChipRadius)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatTime(timeMs), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
