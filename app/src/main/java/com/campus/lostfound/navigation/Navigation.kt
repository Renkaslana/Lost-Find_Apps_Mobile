package com.campus.lostfound.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.campus.lostfound.ui.screen.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Add : Screen("add", "Tambah", Icons.Default.Add)
    object Activity : Screen("activity", "Aktivitas", Icons.Default.List)
    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.Add.route)
                }
            )
        }
        
        composable(Screen.Add.route) {
            AddReportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Activity.route) {
            ActivityScreen()
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

