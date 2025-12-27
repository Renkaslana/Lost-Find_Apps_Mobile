package com.campus.lostfound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.campus.lostfound.navigation.NavigationGraph
import com.campus.lostfound.navigation.Screen
import com.campus.lostfound.ui.components.BottomNavigationBar
import com.campus.lostfound.ui.theme.CampusLostFoundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = navController.currentDestination?.route ?: Screen.Home.route,
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

