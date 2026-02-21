package com.selfhosttinker.timestable.ui.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.domain.model.StudyTask
import com.selfhosttinker.timestable.ui.components.GradientText
import com.selfhosttinker.timestable.ui.components.pulsating
import com.selfhosttinker.timestable.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val allTasks  by viewModel.allTasks.collectAsStateWithLifecycle()
    val settings  by viewModel.settings.collectAsStateWithLifecycle()
    val gradeScale = remember(settings.gradeScale) { GradeScale.fromId(settings.gradeScale) }

    val pending   = allTasks.filter { !it.isCompleted }
    val completed = allTasks.filter {  it.isCompleted }

    // Task waiting for grade entry
    var taskForGrade by remember { mutableStateOf<StudyTask?>(null) }
    val haptic = LocalHapticFeedback.current

    taskForGrade?.let { task ->
        GradeEntryDialog(
            gradeScale = gradeScale,
            onDismiss  = { taskForGrade = null },
            onSkip     = {
                viewModel.completeTask(task, null)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                taskForGrade = null
            },
            onConfirm  = { grade ->
                viewModel.completeTask(task, grade)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                taskForGrade = null
            }
        )
    }

    if (allTasks.isEmpty()) {
        EmptyTasksState(modifier = Modifier.fillMaxSize())
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (pending.isNotEmpty()) {
                item {
                    Text(
                        "To Do",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(pending, key = { it.id }) { task ->
                    SwipeableTaskRow(
                        task        = task,
                        gradeScale  = gradeScale,
                        onComplete  = { taskForGrade = task },
                        onDelete    = { viewModel.deleteTask(task) }
                    )
                }
            }

            if (completed.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(completed, key = { it.id }) { task ->
                    SwipeableTaskRow(
                        task        = task,
                        gradeScale  = gradeScale,
                        onUndo      = { viewModel.uncompleteTask(task) },
                        onDelete    = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

// ── Swipeable wrapper (left = delete, right = complete/undo) ─────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskRow(
    task: StudyTask,
    gradeScale: GradeScale,
    onComplete: (() -> Unit)? = null,
    onUndo: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (task.isCompleted) onUndo?.invoke() else onComplete?.invoke()
                    false  // snap back, let the dialog/action handle state
                }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val dir = dismissState.dismissDirection
            val bg  = when (dir) {
                SwipeToDismissBoxValue.StartToEnd -> Emerald
                SwipeToDismissBoxValue.EndToStart -> CoralRed
                else -> Color.Transparent
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg, RoundedCornerShape(CardRadius))
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (dir == SwipeToDismissBoxValue.StartToEnd)
                    Arrangement.Start else Arrangement.End
            ) {
                Text(
                    text = when (dir) {
                        SwipeToDismissBoxValue.StartToEnd -> if (task.isCompleted) "Undo" else "Done"
                        SwipeToDismissBoxValue.EndToStart -> "Delete"
                        else -> ""
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        TaskCard(task = task, gradeScale = gradeScale, onComplete = onComplete, onUndo = onUndo)
    }
}

// ── Task card with visible checkbox ─────────────────────────────────────────

@Composable
private fun TaskCard(
    task: StudyTask,
    gradeScale: GradeScale,
    onComplete: (() -> Unit)? = null,
    onUndo: (() -> Unit)? = null
) {
    val taskColor  = task.hexColor.toComposeColor()
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox — primary interaction for completing a task
            Checkbox(
                checked  = task.isCompleted,
                onCheckedChange = {
                    if (task.isCompleted) onUndo?.invoke() else onComplete?.invoke()
                }
            )

            Spacer(Modifier.width(6.dp))

            // Colored strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(taskColor, RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.width(10.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                task.detail?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = dateFormat.format(Date(task.dueDateMs)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    if (task.subjectName.isNotEmpty()) {
                        Text(
                            text  = task.subjectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = taskColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Grade chip
            task.grade?.let { grade ->
                val perf  = gradeScale.performance(grade)
                val color = gradeColor(perf)
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(PillRadius))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = gradeScale.displayValue(grade),
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── Grade entry dialog — adapts to letter vs numeric scales ──────────────────

@Composable
private fun GradeEntryDialog(
    gradeScale: GradeScale,
    onDismiss: () -> Unit,
    onSkip:    () -> Unit,
    onConfirm: (Double?) -> Unit
) {
    var gradeText      by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Pair<String, Double>?>(null) }

    val parsedGrade = gradeText.toDoubleOrNull()
    val isOutOfRange = parsedGrade != null && (parsedGrade < gradeScale.min || parsedGrade > gradeScale.max)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Task") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Add a grade (optional)")

                if (gradeScale.isLetter) {
                    // Button grid for letter grades
                    val options = gradeScale.letterOptions ?: emptyList()
                    val cols = 5
                    for (rowStart in options.indices step cols) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (i in rowStart until minOf(rowStart + cols, options.size)) {
                                val (label, value) = options[i]
                                val isSelected = selectedLetter?.second == value
                                OutlinedButton(
                                    onClick = { selectedLetter = options[i] },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Numeric text input
                    OutlinedTextField(
                        value = gradeText,
                        onValueChange = { gradeText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Grade (${gradeScale.min.toInt()}–${gradeScale.max.toInt()})") },
                        singleLine = true,
                        isError = isOutOfRange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isOutOfRange) {
                        Text(
                            text = "Must be ${gradeScale.min.toInt()}–${gradeScale.max.toInt()}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val grade = if (gradeScale.isLetter) selectedLetter?.second
                                else parsedGrade?.coerceIn(gradeScale.min, gradeScale.max)
                    onConfirm(grade)
                },
                enabled = !isOutOfRange
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip)    { Text("Skip") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyTasksState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Checklist,
            contentDescription = null,
            modifier = Modifier.size(64.dp).pulsating(),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        GradientText(
            text = "No Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add tasks from a class detail view",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
