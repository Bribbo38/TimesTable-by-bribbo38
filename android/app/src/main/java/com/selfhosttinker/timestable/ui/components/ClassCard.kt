package com.selfhosttinker.timestable.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.ui.theme.CardRadius
import com.selfhosttinker.timestable.ui.theme.toComposeColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClassCard(
    schoolClass: SchoolClass,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classColor = schoolClass.hexColor.toComposeColor()
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(CardRadius))
            .clip(RoundedCornerShape(CardRadius))
            .background(classColor.copy(alpha = 0.08f))
            .clickable {
                pressed = true
                onClick()
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(classColor)
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = schoolClass.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${formatTime(schoolClass.startTimeMs)}–${formatTime(schoolClass.endTimeMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    schoolClass.room?.let { room ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Outlined.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = room,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    schoolClass.teacher?.let { teacher ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = teacher,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Reset pressed state after a short delay
        LaunchedEffect(pressed) {
            if (pressed) {
                kotlinx.coroutines.delay(150)
                pressed = false
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

fun formatTime(epochMs: Long): String {
    // Only extract time portion: treat epochMs as millis-since-midnight
    val totalMinutes = (epochMs / 60_000).toInt()
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60
    return "%02d:%02d".format(hours, minutes)
}
