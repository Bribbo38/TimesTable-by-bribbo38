package com.selfhosttinker.timestable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.selfhosttinker.timestable.ui.navigation.AppNavigation
import com.selfhosttinker.timestable.ui.navigation.AppNavigationViewModel
import com.selfhosttinker.timestable.ui.navigation.NavOnboardingScreen
import com.selfhosttinker.timestable.ui.theme.*
import com.selfhosttinker.timestable.widget.NowNextWidget
import com.selfhosttinker.timestable.widget.TimetableWidget
import androidx.glance.appwidget.updateAll
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
                GradientBox {
                    val navViewModel: AppNavigationViewModel = hiltViewModel()
                    val settings by navViewModel.settings.collectAsStateWithLifecycle()
                    when {
                        !settings.navStyleChosen -> NavOnboardingScreen(onChoose = navViewModel::setNavStyle)
                        else -> AppNavigation(navViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientBox(content: @Composable () -> Unit) {
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
        content()
    }
}
