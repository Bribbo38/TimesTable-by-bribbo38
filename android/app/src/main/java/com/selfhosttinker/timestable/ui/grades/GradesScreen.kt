package com.selfhosttinker.timestable.ui.grades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.selfhosttinker.timestable.domain.AverageType
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.ui.components.GlassCard
import com.selfhosttinker.timestable.ui.components.GradientText
import com.selfhosttinker.timestable.ui.components.pulsating
import com.selfhosttinker.timestable.ui.theme.*

@Composable
fun GradesScreen(
    viewModel: GradesViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val subjectGrades by viewModel.subjectGrades.collectAsStateWithLifecycle()
    val overallAverage by viewModel.overallAverage.collectAsStateWithLifecycle()

    if (subjectGrades.isEmpty()) {
        EmptyGradesState(modifier = Modifier.fillMaxSize())
        return
    }

    val avgType = AverageType.fromRawValue(settings.averageType)
    val gradeScale = remember(settings.gradeScale) { GradeScale.fromId(settings.gradeScale) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Grades",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Overall average card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Overall Average",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val overallColor = gradeColor(gradeScale.performance(overallAverage))
                    Text(
                        text = gradeScale.displayValue(overallAverage),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = overallColor
                    )
                    Text(
                        text = avgType.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Subject cards
        items(subjectGrades, key = { it.subjectName }) { subject ->
            SubjectCard(subject = subject, gradeScale = gradeScale)
        }
    }
}

@Composable
private fun SubjectCard(subject: SubjectGrades, gradeScale: GradeScale) {
    val subjectColor = subject.hexColor.toComposeColor()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Subject header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${subject.grades.size} grade${if (subject.grades.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = gradeScale.displayValue(subject.average),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor(gradeScale.performance(subject.average))
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Individual grades
            subject.grades.forEach { grade ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = gradeScale.displayValue(grade),
                        style = MaterialTheme.typography.bodyMedium,
                        color = gradeColor(gradeScale.performance(grade)),
                        fontWeight = FontWeight.Medium
                    )
                    val pct = (gradeScale.performance(grade) * 100).toInt()
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGradesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BarChart,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .pulsating(),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GradientText(
            text = "No Grades",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Complete tasks with a grade to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
