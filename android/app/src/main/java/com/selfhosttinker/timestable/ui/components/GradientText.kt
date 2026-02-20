package com.selfhosttinker.timestable.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.selfhosttinker.timestable.ui.theme.BlueIndigoBrush

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    brush: Brush = BlueIndigoBrush,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.merge(
            TextStyle(
                brush = brush,
                fontSize = fontSize,
                fontWeight = fontWeight ?: style.fontWeight
            )
        )
    )
}
