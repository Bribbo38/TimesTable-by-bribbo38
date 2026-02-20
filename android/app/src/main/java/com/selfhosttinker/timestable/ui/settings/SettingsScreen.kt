package com.selfhosttinker.timestable.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.selfhosttinker.timestable.domain.AverageType
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.ui.theme.CoralRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showAddPresetDialog by remember { mutableStateOf(false) }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (json != null) viewModel.importJson(context, json)
        }
    }

    // Notification permission (API 33+)
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Timetable") },
            text = { Text("This will delete all classes, tasks, and presets. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetAll(); showResetDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onConfirm = { name, room, teacher, color ->
                viewModel.addPreset(name, room, teacher, color)
                showAddPresetDialog = false
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // ── Schedule ─────────────────────────────────────────────────────────
        item { SectionHeader("Schedule") }
        item {
            SettingsCard {
                SettingsToggleRow(
                    label = "Show Weekends",
                    checked = settings.showWeekends,
                    onCheckedChange = viewModel::setShowWeekends
                )
                HorizontalDivider()
                SettingsToggleRow(
                    label = "Repeating Weeks",
                    checked = settings.repeatingWeeksEnabled,
                    onCheckedChange = viewModel::setRepeatingWeeks
                )
                if (settings.repeatingWeeksEnabled) {
                    HorizontalDivider()
                    SettingsStepperRow(
                        label = "Number of Weeks",
                        value = settings.numberOfWeeks,
                        range = 1..4,
                        onValueChange = viewModel::setNumberOfWeeks
                    )
                }
            }
        }

        // ── Subjects / Presets ────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Subjects")
                TextButton(onClick = { showAddPresetDialog = true }) { Text("Add") }
            }
        }
        if (presets.isEmpty()) {
            item {
                Text(
                    "No subject presets yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(presets, key = { it.id }) { preset ->
                PresetRow(preset = preset, onDelete = { viewModel.deletePreset(preset) })
            }
        }

        // ── Grades ───────────────────────────────────────────────────────────
        item { SectionHeader("Grades") }
        item {
            SettingsCard {
                // Average type picker
                var avgExpanded by remember { mutableStateOf(false) }
                val currentAvgType = AverageType.fromRawValue(settings.averageType)
                ExposedDropdownMenuBox(
                    expanded = avgExpanded,
                    onExpandedChange = { avgExpanded = !avgExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = currentAvgType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Average Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = avgExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = avgExpanded,
                        onDismissRequest = { avgExpanded = false }
                    ) {
                        AverageType.all.forEach { avgType ->
                            DropdownMenuItem(
                                text = { Text(avgType.displayName) },
                                onClick = {
                                    viewModel.setAverageType(avgType.rawValue)
                                    avgExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Grade system picker
                var rangeExpanded by remember { mutableStateOf(false) }
                val currentGradeScale = remember(settings.gradeScale) { GradeScale.fromId(settings.gradeScale) }
                ExposedDropdownMenuBox(
                    expanded = rangeExpanded,
                    onExpandedChange = { rangeExpanded = !rangeExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = currentGradeScale.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade System") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rangeExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = rangeExpanded,
                        onDismissRequest = { rangeExpanded = false }
                    ) {
                        GradeScale.all.forEach { scale ->
                            DropdownMenuItem(
                                text = { Text(scale.displayName) },
                                onClick = {
                                    viewModel.setGradeScale(scale.id)
                                    rangeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Notifications ─────────────────────────────────────────────────────
        item { SectionHeader("Notifications") }
        item {
            SettingsCard {
                SettingsToggleRow(
                    label = "Enable Class Reminders",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && notificationPermission != null && !notificationPermission.status.isGranted) {
                            notificationPermission.launchPermissionRequest()
                        } else {
                            viewModel.setNotificationsEnabled(enabled)
                        }
                    }
                )
            }
        }

        // ── Backup ────────────────────────────────────────────────────────────
        item { SectionHeader("Backup") }
        item {
            SettingsCard {
                SettingsButtonRow(label = "Export as JSON") { viewModel.exportJson(context) }
                HorizontalDivider()
                SettingsButtonRow(label = "Import from JSON") {
                    importLauncher.launch(arrayOf("application/json", "text/plain"))
                }
            }
        }

        // ── Reset ─────────────────────────────────────────────────────────────
        item { SectionHeader("Reset") }
        item {
            SettingsCard {
                SettingsButtonRow(
                    label = "Reset Timetable",
                    destructive = true
                ) { showResetDialog = true }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsStepperRow(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                enabled = value > range.first
            ) { Text("−") }
            Text(value.toString(), style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                enabled = value < range.last
            ) { Text("+") }
        }
    }
}

@Composable
private fun SettingsButtonRow(
    label: String,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructive) CoralRed else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(label, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun PresetRow(preset: ClassPreset, onDelete: () -> Unit) {
    val presetColor = preset.hexColor.toComposeColor()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(presetColor, shape = CircleShape)
                )
                Column {
                    Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    val sub = listOfNotNull(preset.room, preset.teacher).joinToString(" · ")
                    if (sub.isNotEmpty()) {
                        Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete preset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddPresetDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, room: String?, teacher: String?, hexColor: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var hexColor by remember { mutableStateOf("#0A84FF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject Preset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Teacher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                com.selfhosttinker.timestable.ui.components.ColorCircleGrid(
                    selectedColor = hexColor.toComposeColor(),
                    onColorSelected = { color ->
                        val argb = android.graphics.Color.argb(
                            (color.alpha * 255).toInt(),
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                        hexColor = "#%06X".format(argb and 0xFFFFFF)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            room.trim().ifEmpty { null },
                            teacher.trim().ifEmpty { null },
                            hexColor
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Helper extension needed here too
private fun String.toComposeColor(): androidx.compose.ui.graphics.Color = try {
    val hex = this.trimStart('#')
    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#$hex"))
} catch (e: Exception) {
    com.selfhosttinker.timestable.ui.theme.ElectricBlue
}
