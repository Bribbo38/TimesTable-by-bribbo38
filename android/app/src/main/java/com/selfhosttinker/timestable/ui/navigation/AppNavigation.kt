package com.selfhosttinker.timestable.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.selfhosttinker.timestable.ui.addclass.AddEditClassScreen
import com.selfhosttinker.timestable.ui.classdetail.ClassDetailScreen
import com.selfhosttinker.timestable.ui.grades.GradesScreen
import com.selfhosttinker.timestable.ui.schedule.ScheduleScreen
import com.selfhosttinker.timestable.ui.settings.SettingsScreen
import com.selfhosttinker.timestable.ui.tasks.TaskListScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Schedule : Screen("schedule",  "Schedule",  Icons.Outlined.CalendarMonth)
    object Tasks    : Screen("tasks",     "Tasks",     Icons.Outlined.Checklist)
    object Grades   : Screen("grades",   "Grades",    Icons.Outlined.BarChart)
    object Settings : Screen("settings", "Settings",  Icons.Outlined.Settings)
}

private val bottomNavItems = listOf(
    Screen.Schedule, Screen.Tasks, Screen.Grades, Screen.Settings
)

// Non-tab routes
private const val ROUTE_ADD_CLASS    = "add_class?day={dayOfWeek}"
private const val ROUTE_EDIT_CLASS   = "edit_class/{classId}"
private const val ROUTE_CLASS_DETAIL = "class_detail/{classId}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navViewModel: AppNavigationViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settings by navViewModel.settings.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val tabRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in tabRoutes && !isLandscape && !settings.useHamburgerNav
    val showHamburgerBar = currentRoute in tabRoutes && settings.useHamburgerNav

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentScreen = bottomNavItems.firstOrNull { it.route == currentRoute }

    val navHostContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onNavigateToAddClass = { day -> navController.navigate("add_class?day=$day") },
                    onNavigateToClassDetail = { classId ->
                        navController.navigate("class_detail/$classId")
                    }
                )
            }
            composable(Screen.Tasks.route) {
                TaskListScreen()
            }
            composable(Screen.Grades.route) {
                GradesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = ROUTE_ADD_CLASS,
                arguments = listOf(navArgument("dayOfWeek") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                AddEditClassScreen(
                    classId = null,
                    initialDay = backStackEntry.arguments?.getInt("dayOfWeek") ?: 0,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = ROUTE_EDIT_CLASS,
                arguments = listOf(navArgument("classId") { type = NavType.StringType })
            ) { backStackEntry ->
                AddEditClassScreen(
                    classId = backStackEntry.arguments?.getString("classId"),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = ROUTE_CLASS_DETAIL,
                arguments = listOf(navArgument("classId") { type = NavType.StringType })
            ) { backStackEntry ->
                ClassDetailScreen(
                    classId = backStackEntry.arguments?.getString("classId") ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { classId ->
                        navController.navigate("edit_class/$classId")
                    }
                )
            }
        }
    }

    if (showHamburgerBar) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "TimesTable+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    bottomNavItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(currentScreen?.label ?: "") },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                            }
                        }
                    )
                }
            ) { innerPadding -> navHostContent(innerPadding) }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding -> navHostContent(innerPadding) }
    }
}
