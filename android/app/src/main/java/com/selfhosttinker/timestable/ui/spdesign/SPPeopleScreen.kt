package com.selfhosttinker.timestable.ui.spdesign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.domain.model.Teacher
import com.selfhosttinker.timestable.ui.theme.SPCardRadius
import com.selfhosttinker.timestable.ui.theme.toComposeColor

@Composable
fun SPPeopleScreen(
    viewModel: SPPeopleViewModel = hiltViewModel(),
    onNavigateToSubjectDetail: (String) -> Unit,
    onNavigateToTeacherDetail: (String) -> Unit
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val allTeachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var deleteSubjectTarget by remember { mutableStateOf<ClassPresetWithTeachers?>(null) }
    var deleteTeacherTarget by remember { mutableStateOf<TeacherWithSubjects?>(null) }

    // Delete confirmation dialogs
    deleteSubjectTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteSubjectTarget = null },
            title = { Text("Delete Subject") },
            text = { Text("Delete \"${target.preset.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteSubject(target.preset); deleteSubjectTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteSubjectTarget = null }) { Text("Cancel") }
            }
        )
    }

    deleteTeacherTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTeacherTarget = null },
            title = { Text("Delete Teacher") },
            text = { Text("Delete \"${target.teacher.fullName}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteTeacher(target.teacher.id); deleteTeacherTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTeacherTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            allTeachers = allTeachers,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { preset, teacherIds ->
                viewModel.saveSubject(preset, teacherIds)
                showAddSubjectDialog = false
            }
        )
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onConfirm = { firstName, lastName ->
                viewModel.saveTeacher(firstName, lastName)
                showAddTeacherDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddSubjectDialog = true else showAddTeacherDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Title
            Text(
                text = "People",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Subjects") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Teachers") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> SPSubjectsTab(
                    subjects = subjects,
                    classes = classes,
                    onCardClick = { onNavigateToSubjectDetail(it.preset.id) },
                    onDelete = { deleteSubjectTarget = it }
                )
                1 -> SPTeachersTab(
                    teachers = teachers,
                    onRowClick = { onNavigateToTeacherDetail(it.teacher.id) },
                    onDelete = { deleteTeacherTarget = it }
                )
            }
        }
    }
}

// ── Subjects tab ──────────────────────────────────────────────────────────────

@Composable
private fun SPSubjectsTab(
    subjects: List<ClassPresetWithTeachers>,
    classes: List<SchoolClass>,
    onCardClick: (ClassPresetWithTeachers) -> Unit,
    onDelete: (ClassPresetWithTeachers) -> Unit
) {
    if (subjects.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No subjects yet.\nTap + to add one.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(subjects, key = { it.preset.id }) { item ->
            SPSubjectCard(
                item = item,
                classes = classes,
                onClick = { onCardClick(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun SPSubjectCard(
    item: ClassPresetWithTeachers,
    classes: List<SchoolClass>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = item.preset.hexColor.toComposeColor()
    val daysWithClass = remember(classes, item.preset.name) {
        classes.filter { it.name == item.preset.name }.map { it.dayOfWeek }.toSet()
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(SPCardRadius)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(subjectColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item.preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.teachers.isNotEmpty()) {
                    Text(
                        text = item.teachers.joinToString(", ") { it.fullName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Weekday indicators M T W T F
                DayIndicatorRow(activeDays = daysWithClass, accentColor = subjectColor)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayIndicatorRow(activeDays: Set<Int>, accentColor: Color) {
    val dayLabels = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dayLabels.forEach { (label, dayNum) ->
            val active = dayNum in activeDays
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (active) accentColor else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (active) accentColor else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ── Teachers tab ──────────────────────────────────────────────────────────────

@Composable
private fun SPTeachersTab(
    teachers: List<TeacherWithSubjects>,
    onRowClick: (TeacherWithSubjects) -> Unit,
    onDelete: (TeacherWithSubjects) -> Unit
) {
    if (teachers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No teachers yet.\nTap + to add one.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Group by first letter of lastName
    val grouped = remember(teachers) {
        teachers
            .sortedWith(compareBy({ it.teacher.lastName }, { it.teacher.firstName }))
            .groupBy { it.teacher.lastName.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        grouped.forEach { (letter, group) ->
            item(key = "header_$letter") {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp)
                )
            }
            items(group, key = { it.teacher.id }) { item ->
                SPTeacherRow(
                    item = item,
                    onClick = { onRowClick(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
private fun SPTeacherRow(
    item: TeacherWithSubjects,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(SPCardRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.teacher.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (item.subjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.subjects.take(4).forEach { subject ->
                            SubjectChip(preset = subject)
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubjectChip(preset: ClassPreset) {
    val color = preset.hexColor.toComposeColor()
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Add Subject dialog ────────────────────────────────────────────────────────

@Composable
private fun AddSubjectDialog(
    allTeachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (ClassPreset, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var hexColor by remember { mutableStateOf("#1565C0") }
    var selectedTeacherIds by remember { mutableStateOf(emptySet<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject") },
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
                // Teacher multi-select
                if (allTeachers.isEmpty()) {
                    Text(
                        text = "No teachers yet — add in Teachers tab",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Teachers",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    allTeachers.forEach { teacher ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = teacher.id in selectedTeacherIds,
                                onCheckedChange = { checked ->
                                    selectedTeacherIds = if (checked) {
                                        selectedTeacherIds + teacher.id
                                    } else {
                                        selectedTeacherIds - teacher.id
                                    }
                                }
                            )
                            Text(
                                text = teacher.fullName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            ClassPreset(
                                name = name.trim(),
                                room = room.trim().ifEmpty { null },
                                hexColor = hexColor
                            ),
                            selectedTeacherIds.toList()
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

// ── Add Teacher dialog ────────────────────────────────────────────────────────

@Composable
private fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, lastName: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Teacher") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (firstName.isNotBlank() || lastName.isNotBlank()) {
                        onConfirm(firstName.trim(), lastName.trim())
                    }
                },
                enabled = firstName.isNotBlank() || lastName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
