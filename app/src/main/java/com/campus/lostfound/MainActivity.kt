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
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campus.lostfound.navigation.NavigationGraph
import com.campus.lostfound.navigation.Screen
import com.campus.lostfound.ui.components.BottomNavigationBar
import com.campus.lostfound.ui.theme.CampusLostFoundTheme

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
            CampusLostFoundTheme {
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
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Home.route
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
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
                }
            )
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

