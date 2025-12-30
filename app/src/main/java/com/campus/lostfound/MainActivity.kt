package com.campus.lostfound

import android.os.Bundle
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campus.lostfound.data.SettingsRepository
import com.campus.lostfound.navigation.NavigationGraph
import com.campus.lostfound.navigation.Screen
import com.campus.lostfound.ui.components.BottomNavigationBar
import com.campus.lostfound.ui.theme.CampusLostFoundTheme
import com.campus.lostfound.ui.theme.ThemeColor
import com.campus.lostfound.ui.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize per-device lastSeen on first run so new installs don't see old notifications
        try {
            val prefs = getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            if (!prefs.contains("lastSeen")) {
                prefs.edit().putLong("lastSeen", System.currentTimeMillis()).apply()
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to init lastSeen", e)
        }

        // Subscribe to global topic for broadcast notifications
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) Log.w("MainActivity", "Topic subscribe failed", task.exception)
            }
        setContent {
            // Collect theme settings
            val context = LocalContext.current
            val settingsRepository = SettingsRepository(context)
            val themeMode by settingsRepository.themeModeFlow.collectAsState(initial = "system")
            val themeColorName by settingsRepository.themeColorFlow.collectAsState(initial = "DEFAULT")
            
            val themeColor = try {
                ThemeColor.valueOf(themeColorName)
            } catch (e: Exception) {
                ThemeColor.DEFAULT
            }
            
            CampusLostFoundTheme(
                themeMode = themeMode,
                themeColor = themeColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Home.route
    
    // Notification ViewModel for badge count
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationViewModel(context) as T
            }
        }
    )
    val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.read }
    
    // Only show bottom navigation on main app sections
    val showBottomBar = when {
        currentRoute == Screen.Home.route -> true
        currentRoute == Screen.Add.route -> true
        currentRoute == Screen.Activity.route -> true
        currentRoute == Screen.Settings.route -> true
        currentRoute == Screen.Explore.route -> true
        // detail has parameter like "detail/{itemId}", match prefix
        currentRoute?.startsWith("detail") == true -> false
        currentRoute == Screen.Notifications.route -> false
        else -> false
    }

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    notificationBadgeCount = unreadCount
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavigationGraph(navController = navController)
        }
    }
}

