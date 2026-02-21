package com.selfhosttinker.timestable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.lifecycleScope
import com.selfhosttinker.timestable.ui.navigation.AppNavigation
import com.selfhosttinker.timestable.ui.theme.*
import androidx.glance.appwidget.updateAll
import com.selfhosttinker.timestable.widget.NowNextWidget
import com.selfhosttinker.timestable.widget.TimetableWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            TimetableWidget().updateAll(this@MainActivity)
            NowNextWidget().updateAll(this@MainActivity)
        }
        setContent {
            AppTheme {
                val isDark = isSystemInDarkTheme()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                if (isDark) listOf(DarkBgStart, DarkBgEnd)
                                else listOf(LightBgStart, LightBgEnd)
                            )
                        )
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
