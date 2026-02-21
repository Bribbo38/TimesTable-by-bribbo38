package com.selfhosttinker.timestable.ui.spdesign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.GradeEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SPAddEditGradeScreen(
    initialEntry: GradeEntry? = null,
    presets: List<ClassPreset>,
    gradeScale: GradeScale,
    onSave: (GradeEntry) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isEdit = initialEntry != null

    // Form state
    var subjectName by remember { mutableStateOf(initialEntry?.subjectName ?: "") }
    var subjectNameFree by remember { mutableStateOf(initialEntry?.subjectName ?: "") }
    var selectedPreset by remember { mutableStateOf<ClassPreset?>(null) }
    var valueText by remember {
        mutableStateOf(if (initialEntry != null) gradeScale.displayValue(initialEntry.value) else "")
    }
    var weight by remember { mutableStateOf(initialEntry?.weight ?: 1.0) }
    var labelText by remember { mutableStateOf(initialEntry?.label ?: "") }
    var dateMs by remember { mutableStateOf(initialEntry?.dateMs ?: System.currentTimeMillis()) }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val weightOptions = listOf(0.5, 1.0, 1.5, 2.0, 3.0)

    val parsedValue = valueText.toDoubleOrNull()
    val isOutOfRange = parsedValue != null && (parsedValue < gradeScale.min || parsedValue > gradeScale.max)
    val canSave = subjectName.isNotBlank() && parsedValue != null && !isOutOfRange

    // Date picker state
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMs)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMs = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Grade" else "Add Grade",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val entry = GradeEntry(
                                id = initialEntry?.id ?: "",
                                presetId = selectedPreset?.id ?: initialEntry?.presetId,
                                subjectName = if (selectedPreset != null) selectedPreset!!.name else subjectNameFree,
                                hexColor = selectedPreset?.hexColor ?: initialEntry?.hexColor ?: "#1565C0",
                                value = parsedValue ?: 0.0,
                                weight = weight,
                                dateMs = dateMs,
                                label = labelText.trim().ifEmpty { null }
                            )
                            onSave(entry)
                            onNavigateBack()
                        },
                        enabled = canSave
                    ) {
                        Text("Save")
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
            // Subject picker — dropdown of presets + free-text option
            ExposedDropdownMenuBox(
                expanded = subjectDropdownExpanded,
                onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = {
                        subjectName = it
                        subjectNameFree = it
                        selectedPreset = null
                    },
                    label = { Text("Subject *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )
                if (presets.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        presets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name) },
                                onClick = {
                                    selectedPreset = preset
                                    subjectName = preset.name
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Grade value field
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Grade (${gradeScale.min.toInt()}–${gradeScale.max.toInt()}) *") },
                singleLine = true,
                isError = isOutOfRange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = if (isOutOfRange) {
                    { Text("Must be ${gradeScale.min.toInt()}–${gradeScale.max.toInt()}") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            // Weight selector
            Column {
                Text(
                    "Weight",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weightOptions.forEach { w ->
                        FilterChip(
                            selected = weight == w,
                            onClick = { weight = w },
                            label = { Text(if (w == w.toLong().toDouble()) "×${w.toInt()}" else "×$w") }
                        )
                    }
                }
            }

            // Date picker button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${dateFormat.format(Date(dateMs))}")
            }

            // Optional label field
            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                label = { Text("Label (optional, e.g. Midterm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
