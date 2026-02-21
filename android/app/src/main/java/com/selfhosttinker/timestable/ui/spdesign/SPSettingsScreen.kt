package com.selfhosttinker.timestable.ui.spdesign

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.selfhosttinker.timestable.ui.settings.SettingsViewModel
import com.selfhosttinker.timestable.ui.theme.CoralRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SPSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            if (json != null) viewModel.importJson(context, json)
        }
    }

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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // ── Appearance ──────────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Appearance") {
                SPSettingsToggleRow(
                    label = "School Planner Style",
                    sublabel = "Restart app to apply",
                    checked = settings.useSchoolPlannerTheme,
                    onCheckedChange = viewModel::setUseSpTheme
                )
            }
        }

        // ── Schedule ────────────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Schedule") {
                SPSettingsToggleRow(
                    label = "Show Weekends",
                    checked = settings.showWeekends,
                    onCheckedChange = viewModel::setShowWeekends
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SPSettingsToggleRow(
                    label = "Repeating Weeks",
                    checked = settings.repeatingWeeksEnabled,
                    onCheckedChange = viewModel::setRepeatingWeeks
                )
                if (settings.repeatingWeeksEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SPSettingsStepperRow(
                        label = "Number of Weeks",
                        value = settings.numberOfWeeks,
                        range = 1..4,
                        onValueChange = viewModel::setNumberOfWeeks
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                var durationExpanded by remember { mutableStateOf(false) }
                val durationOptions = listOf(30, 45, 60, 90, 120)
                ExposedDropdownMenuBox(
                    expanded = durationExpanded,
                    onExpandedChange = { durationExpanded = !durationExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = "${settings.defaultClassDurationMin} min",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Class Duration") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = durationExpanded,
                        onDismissRequest = { durationExpanded = false }
                    ) {
                        durationOptions.forEach { minutes ->
                            DropdownMenuItem(
                                text = { Text("$minutes min") },
                                onClick = {
                                    viewModel.setDefaultClassDurationMin(minutes)
                                    durationExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Grades ──────────────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Grades") {
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
                                onClick = { viewModel.setAverageType(avgType.rawValue); avgExpanded = false }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                                onClick = { viewModel.setGradeScale(scale.id); rangeExpanded = false }
                            )
                        }
                    }
                }
            }
        }

        // ── Notifications ───────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Notifications") {
                SPSettingsToggleRow(
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

        // ── Backup ──────────────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Backup") {
                SPSettingsButtonRow(label = "Export as JSON") { viewModel.exportJson(context) }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SPSettingsButtonRow(label = "Import from JSON") {
                    importLauncher.launch(arrayOf("application/json", "text/plain"))
                }
            }
        }

        // ── Reset ────────────────────────────────────────────────────────────────
        item {
            SPSectionCard(title = "Reset") {
                SPSettingsButtonRow(label = "Reset Timetable", destructive = true) {
                    showResetDialog = true
                }
            }
        }
    }
}

// ── SP-style section card with accent header strip ────────────────────────────

@Composable
private fun SPSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        // Section header with accent
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        // Content card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SPSettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    sublabel: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (sublabel != null) {
                Text(
                    sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SPSettingsStepperRow(
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
private fun SPSettingsButtonRow(
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

