package com.selfhosttinker.timestable.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.selfhosttinker.timestable.ui.theme.CardRadius

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                Color.White.copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isDark) Color.White.copy(alpha = 0.12f)
                    else Color.White.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}
