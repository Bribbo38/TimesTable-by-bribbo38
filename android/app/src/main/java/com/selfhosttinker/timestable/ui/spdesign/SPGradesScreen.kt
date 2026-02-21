package com.selfhosttinker.timestable.ui.spdesign

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.domain.model.GradeEntry
import com.selfhosttinker.timestable.ui.theme.SPCardElevation
import com.selfhosttinker.timestable.ui.theme.SPCardRadius
import com.selfhosttinker.timestable.ui.theme.gradeColor
import com.selfhosttinker.timestable.ui.theme.toComposeColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SPGradesScreen(
    viewModel: SPGradesViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val subjectGroups by viewModel.subjectGroups.collectAsStateWithLifecycle()
    val overallAverage by viewModel.overallAverage.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val gradeScale = remember(settings.gradeScale) { GradeScale.fromId(settings.gradeScale) }

    var showAddGrade by remember { mutableStateOf(false) }
    var addForSubject by remember { mutableStateOf<SubjectGradeGroup?>(null) }
    var editingEntry by remember { mutableStateOf<GradeEntry?>(null) }

    if (showAddGrade || addForSubject != null || editingEntry != null) {
        SPAddEditGradeScreen(
            initialEntry = editingEntry ?: addForSubject?.let { group ->
                GradeEntry(
                    subjectName = group.subjectName,
                    hexColor = group.hexColor
                )
            },
            presets = presets,
            gradeScale = gradeScale,
            onSave = { viewModel.saveEntry(it) },
            onNavigateBack = {
                showAddGrade = false
                addForSubject = null
                editingEntry = null
            }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGrade = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add grade entry")
            }
        }
    ) { paddingValues ->
        if (subjectGroups.isEmpty()) {
            SPEmptyGradesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Grades",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Overall average card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = SPCardElevation),
                        shape = RoundedCornerShape(SPCardRadius)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Overall Average",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (overallAverage.isNaN()) {
                                Text(
                                    text = "—",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = gradeScale.displayValue(overallAverage),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = gradeColor(gradeScale.performance(overallAverage))
                                )
                            }
                            Text(
                                text = "Weighted · ${subjectGroups.size} subject${if (subjectGroups.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Subject cards
                items(subjectGroups, key = { it.subjectName }) { group ->
                    SPSubjectCard(
                        group = group,
                        gradeScale = gradeScale,
                        onAddEntry = { addForSubject = group },
                        onDeleteEntry = { viewModel.deleteEntry(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SPSubjectCard(
    group: SubjectGradeGroup,
    gradeScale: GradeScale,
    onAddEntry: () -> Unit,
    onDeleteEntry: (GradeEntry) -> Unit
) {
    val subjectColor = group.hexColor.toComposeColor()
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = SPCardElevation),
        shape = RoundedCornerShape(SPCardRadius)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subject header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
                Text(
                    text = group.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (!group.weightedAverage.isNaN()) {
                    Text(
                        text = "avg: ${gradeScale.displayValue(group.weightedAverage)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor(gradeScale.performance(group.weightedAverage))
                    )
                }
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded entries
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    group.entries.forEach { entry ->
                        GradeEntryRow(
                            entry = entry,
                            gradeScale = gradeScale,
                            dateFormat = dateFormat,
                            onDelete = { onDeleteEntry(entry) }
                        )
                    }
                    // Add entry button for this subject
                    TextButton(
                        onClick = onAddEntry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Grade")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeEntryRow(
    entry: GradeEntry,
    gradeScale: GradeScale,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label ?: dateFormat.format(Date(entry.dateMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(entry.dateMs)) + " · ×${
                        if (entry.weight == entry.weight.toLong().toDouble())
                            entry.weight.toInt() else entry.weight
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = gradeScale.displayValue(entry.value),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = gradeColor(gradeScale.performance(entry.value))
            )
        }
    }
}

@Composable
private fun SPEmptyGradesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Grades Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first grade entry",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
