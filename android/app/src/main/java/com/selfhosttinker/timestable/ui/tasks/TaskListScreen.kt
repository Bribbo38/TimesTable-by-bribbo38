package com.selfhosttinker.timestable.ui.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val pending   = allTasks.filter { !it.isCompleted }
    val completed = allTasks.filter { it.isCompleted }

    var taskForGrade by remember { mutableStateOf<StudyTask?>(null) }
    val haptic = LocalHapticFeedback.current

    taskForGrade?.let { task ->
        GradeEntryDialog(
            onDismiss = { taskForGrade = null },
            onSkip = {
                viewModel.completeTask(task, null)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                taskForGrade = null
            },
            onConfirm = { grade ->
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
                    TaskRow(
                        task = task,
                        onComplete = { taskForGrade = task },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            if (completed.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(completed, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onUndo = { viewModel.uncompleteTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskRow(
    task: StudyTask,
    onComplete: (() -> Unit)? = null,
    onUndo: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (task.isCompleted) onUndo?.invoke() else onComplete?.invoke()
                    false // don't auto-dismiss, let dialog handle it
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> Emerald
                            SwipeToDismissBoxValue.EndToStart -> CoralRed
                            else -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (direction == SwipeToDismissBoxValue.StartToEnd)
                    Arrangement.Start else Arrangement.End
            ) {
                Text(
                    text = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd ->
                            if (task.isCompleted) "Undo" else "Done"
                        SwipeToDismissBoxValue.EndToStart -> "Delete"
                        else -> ""
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        TaskCard(task = task)
    }
}

@Composable
private fun TaskCard(task: StudyTask) {
    val taskColor = task.hexColor.toComposeColor()
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(taskColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

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
                        text = dateFormat.format(Date(task.dueDateMs)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    if (task.subjectName.isNotEmpty()) {
                        Text(
                            text = task.subjectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = taskColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Grade chip
            task.grade?.let { grade ->
                Box(
                    modifier = Modifier
                        .background(
                            gradeColor(grade, 10).copy(alpha = 0.15f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(PillRadius)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (grade == grade.toLong().toDouble())
                            grade.toLong().toString() else "%.1f".format(grade),
                        color = gradeColor(grade, 10),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeEntryDialog(
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onConfirm: (Double?) -> Unit
) {
    var gradeText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add a grade (optional)")
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = { gradeText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Grade") },
                    placeholder = { Text("e.g. 8.5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val grade = gradeText.toDoubleOrNull()
                    onConfirm(grade)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip) { Text("Skip") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

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
            modifier = Modifier
                .size(64.dp)
                .pulsating(),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GradientText(
            text = "No Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add tasks from a class detail view",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
