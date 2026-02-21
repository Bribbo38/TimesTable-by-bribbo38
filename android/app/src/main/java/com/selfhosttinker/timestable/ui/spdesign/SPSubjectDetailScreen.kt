package com.selfhosttinker.timestable.ui.spdesign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.selfhosttinker.timestable.domain.model.Teacher
import com.selfhosttinker.timestable.ui.theme.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SPSubjectDetailScreen(
    subjectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTeacher: (String) -> Unit,
    viewModel: SPPeopleViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    val item = remember(subjects, subjectId) {
        subjects.firstOrNull { it.preset.id == subjectId }
    }

    if (item == null) {
        // Loading or not found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val preset = item.preset
    val subjectColor = preset.hexColor.toComposeColor()
    val daysWithClass = remember(classes, preset.name) {
        classes.filter { it.name == preset.name }.map { it.dayOfWeek }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = subjectColor.copy(alpha = 0.12f)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Room
            if (!preset.room.isNullOrBlank()) {
                item {
                    DetailSection(title = "Room") {
                        Text(
                            text = preset.room,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Teachers
            item {
                DetailSection(title = "Teachers") {
                    if (item.teachers.isEmpty()) {
                        Text(
                            text = "No teachers assigned",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            item.teachers.forEach { teacher ->
                                TeacherNameRow(
                                    teacher = teacher,
                                    accentColor = subjectColor,
                                    onClick = { onNavigateToTeacher(teacher.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Schedule days
            item {
                DetailSection(title = "Weekly Schedule") {
                    if (daysWithClass.isEmpty()) {
                        Text(
                            text = "No classes scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        SubjectDayRow(activeDays = daysWithClass, accentColor = subjectColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        HorizontalDivider()
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun TeacherNameRow(teacher: Teacher, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Text(
            text = teacher.fullName,
            style = MaterialTheme.typography.bodyLarge,
            color = accentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubjectDayRow(activeDays: Set<Int>, accentColor: Color) {
    val dayLabels = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dayLabels.forEach { (label, dayNum) ->
            val active = dayNum in activeDays
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (active) accentColor else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (active) accentColor else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label.first().toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
