package com.selfhosttinker.timestable.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.selfhosttinker.timestable.ui.addclass.AddEditClassScreen
import com.selfhosttinker.timestable.ui.classdetail.ClassDetailScreen
import com.selfhosttinker.timestable.ui.grades.GradesScreen
import com.selfhosttinker.timestable.ui.schedule.ScheduleScreen
import com.selfhosttinker.timestable.ui.settings.SettingsScreen
import com.selfhosttinker.timestable.ui.tasks.TaskListScreen

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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

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
    ) { innerPadding ->
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
}
