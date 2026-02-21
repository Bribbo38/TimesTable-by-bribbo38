package com.selfhosttinker.timestable.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeOnboardingScreen(onChoose: (useSpTheme: Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Your Design Style",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can change this later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThemeStyleCard(
                modifier = Modifier.weight(1f),
                title = "Classic",
                description = "Gradient cards & glass effects",
                onSelect = { onChoose(false) }
            ) {
                ClassicMockup()
            }
            ThemeStyleCard(
                modifier = Modifier.weight(1f),
                title = "School Planner",
                description = "Clean elevated cards, School Planner-inspired",
                onSelect = { onChoose(true) }
            ) {
                SPMockup()
            }
        }
    }
}

@Composable
private fun ThemeStyleCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onSelect: () -> Unit,
    mockup: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            mockup()
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            FilledTonalButton(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select")
            }
        }
    }
}

// ── Classic mockup: dark card + colored pill day-chips + gradient-style header ─

@Composable
private fun ClassicMockup() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(
                width = 1.5.dp,
                color = Color(0xFF3A3A5C),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Dark background (classic gradient atmosphere)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0C0C1D),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Screen title in electric blue
                Text(
                    text = "Schedule",
                    color = Color(0xFF0A84FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 3.dp)
                )
                // Pill day-chips (gradient/colored pill style)
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("M" to true, "T" to false, "W" to true, "T" to false, "F" to false).forEach { (label, active) ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (active) Color(0xFF0A84FF) else Color(0xFF1E1E38),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else Color(0xFF6B6B8A),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Class card: dark background with gradient-blue subject name
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = Color(0xFF14142A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text(
                            text = "Mathematics",
                            color = Color(0xFF5EB9FF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "08:00  ·  Room A3",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 7.sp
                        )
                    }
                }
                // Bottom nav bar
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFF3A3A5C))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Icons.Outlined.CalendarMonth to true,
                        Icons.Outlined.Checklist to false,
                        Icons.Outlined.BarChart to false,
                        Icons.Outlined.Settings to false
                    ).forEach { (icon, active) ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = if (active) Color(0xFF0A84FF) else Color(0xFF5A5A8A)
                        )
                    }
                }
            }
        }
    }
}

// ── SP mockup: neutral background + ElevatedCard with colored left strip ──────

@Composable
private fun SPMockup() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(
                width = 1.5.dp,
                color = Color(0xFFD1D1D6),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Screen title (plain, bold)
                Text(
                    text = "Schedule",
                    color = Color(0xFF1565C0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 3.dp)
                )
                // Flat day row (outlined chip style)
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf("M" to true, "T" to false, "W" to true, "T" to false, "F" to false).forEach { (label, active) ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (active) Color(0xFFD6E4FF) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (active) Color(0xFF1565C0).copy(alpha = 0.4f) else Color(0xFFD1D1D6),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color(0xFF1565C0) else Color(0xFF9E9E9E),
                                fontSize = 7.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // ElevatedCard with 4dp colored left strip + plain text
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(34.dp)
                                .background(Color(0xFF1565C0))
                        )
                        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
                            Text(
                                text = "Mathematics",
                                color = Color(0xFF1C1C1E),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "08:00  ·  Room A3",
                                color = Color(0xFF636366),
                                fontSize = 7.sp
                            )
                        }
                    }
                }
                // Bottom nav bar (5 icons for SP's 5 tabs)
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Icons.Outlined.CalendarMonth to true,
                        Icons.Outlined.BarChart to false,
                        Icons.Outlined.Checklist to false,
                        Icons.Outlined.Group to false,
                        Icons.Outlined.Settings to false
                    ).forEach { (icon, active) ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = if (active) Color(0xFF1565C0) else Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}
